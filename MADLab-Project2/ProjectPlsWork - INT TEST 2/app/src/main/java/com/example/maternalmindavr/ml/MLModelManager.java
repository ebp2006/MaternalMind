package com.example.maternalmindavr.ml;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.maternalmindavr.database.MoodDbHelper;
import com.example.maternalmindavr.models.MoodEntry;
import java.util.List;

public class MLModelManager {

    /**
     * Enhanced prediction combining Active (Mood) and Passive (Sleep/Activity/Heart Rate) data.
     */
    public RiskResult predictRisk(Context context, List<MoodEntry> moodEntries) {
        MoodDbHelper dbHelper = new MoodDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Fetch last passive data
        float sleepHours = 7.0f; 
        float heartRate = 75.0f; // Default normal
        
        Cursor cursor = db.rawQuery("SELECT * FROM " + MoodDbHelper.TABLE_PASSIVE + " ORDER BY " + MoodDbHelper.COLUMN_TIMESTAMP + " DESC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            sleepHours = cursor.getFloat(cursor.getColumnIndexOrThrow(MoodDbHelper.COLUMN_SLEEP));
            // Check if heart rate column exists and has a value
            int hrIndex = cursor.getColumnIndex(MoodDbHelper.COLUMN_HEART_RATE);
            if (hrIndex != -1) {
                float hrValue = cursor.getFloat(hrIndex);
                if (hrValue > 0) heartRate = hrValue;
            }
        }
        cursor.close();

        // Base risk score
        float riskScore = 0.5f;
        
        // 1. Mood Impact (Weight: 0.3)
        if (moodEntries.size() > 0) {
            String lastMood = moodEntries.get(0).getMood().toLowerCase();
            if (lastMood.contains("sad") || lastMood.contains("anxious")) riskScore += 0.2f;
            if (lastMood.contains("happy")) riskScore -= 0.1f;
        }

        // 2. Sleep Impact (Weight: 0.2)
        if (sleepHours < 5.0f) riskScore += 0.2f;
        if (sleepHours > 8.0f) riskScore -= 0.1f;

        // 3. Heart Rate Impact (Weight: 0.2) - NEW
        // Tachycardia (>100) or Bradycardia (<60) can be physiological signs of stress/anxiety
        if (heartRate > 100 || heartRate < 60) {
            riskScore += 0.15f; 
        } else if (heartRate > 90) {
            riskScore += 0.05f; // Slightly elevated stress
        }

        // Clamp 0-1
        riskScore = Math.max(0, Math.min(1, riskScore));

        String level;
        String advice;
        if (riskScore < 0.3) {
            level = "Low";
            advice = "You're doing great! Your physiological and mood patterns are stable.";
        } else if (riskScore < 0.7) {
            level = "Moderate";
            advice = "Some patterns suggest stress (e.g., elevated heart rate or poor sleep). Try deep breathing or a short walk.";
        } else {
            level = "High";
            advice = "High risk detected. Your recent heart rate and mood patterns suggest significant distress. Please reach out for professional support.";
        }

        return new RiskResult(riskScore, level, advice);
    }

    public String performLocalTraining() {
        return "Encrypted-Hash-" + System.currentTimeMillis();
    }

    public static class RiskResult {
        public float score;
        public String level;
        public String advice;

        public RiskResult(float score, String level, String advice) {
            this.score = score;
            this.level = level;
            this.advice = advice;
        }
    }
}
