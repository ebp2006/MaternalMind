package com.example.maternalmindavr.utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import com.example.maternalmindavr.database.MoodDbHelper;
import java.util.Random;

/**
 * Simulates the collection of passive sensor data (Sleep, Activity, Screen Time).
 * In a real-world app, this would interface with Android Sensor APIs or Google Fit.
 */
public class PassiveDataCollector {
    private static final String TAG = "PassiveDataCollector";

    public static void collectAndStoreData(Context context) {
        MoodDbHelper dbHelper = new MoodDbHelper(context);
        Random random = new Random();

        // Simulate sleep hours (4.0 to 9.0)
        float sleepHours = 4.0f + random.nextFloat() * 5.0f;
        
        // Simulate activity levels
        String[] levels = {"Low", "Moderate", "High"};
        String activityLevel = levels[random.nextInt(levels.length)];
        
        // Simulate screen time hours (1.0 to 6.0)
        float screenTime = 1.0f + random.nextFloat() * 5.0f;

        ContentValues values = new ContentValues();
        values.put(MoodDbHelper.COLUMN_SLEEP, sleepHours);
        values.put(MoodDbHelper.COLUMN_ACTIVITY, activityLevel);
        values.put(MoodDbHelper.COLUMN_SCREEN_TIME, screenTime);
        values.put(MoodDbHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());

        dbHelper.insertData(MoodDbHelper.TABLE_PASSIVE, values);
        Log.d(TAG, "Passive data collected: Sleep=" + sleepHours + ", Activity=" + activityLevel);
    }
}
