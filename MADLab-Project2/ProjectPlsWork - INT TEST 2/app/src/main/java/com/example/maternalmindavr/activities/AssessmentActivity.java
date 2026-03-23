package com.example.maternalmindavr.activities;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.maternalmindavr.R;
import com.example.maternalmindavr.database.MoodDbHelper;

public class AssessmentActivity extends AppCompatActivity {
    private RadioGroup[] groups;
    private Button btnSubmit;
    private TextView tvResult;
    private View cardResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);

        // Initialize all 9 question groups
        groups = new RadioGroup[]{
            findViewById(R.id.rgAge),
            findViewById(R.id.rgSad),
            findViewById(R.id.rgIrritable),
            findViewById(R.id.rgSleep),
            findViewById(R.id.rgConcentration),
            findViewById(R.id.rgAppetite),
            findViewById(R.id.rgGuilt),
            findViewById(R.id.rgBonding),
            findViewById(R.id.rgSuicide)
        };

        btnSubmit = findViewById(R.id.btnSubmitAssessment);
        tvResult = findViewById(R.id.tvAssessmentResult);
        cardResult = findViewById(R.id.cardResult);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> calculateScore());
    }

    private void calculateScore() {
        int totalScore = 0;
        for (RadioGroup rg : groups) {
            int selectedId = rg.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }
            View radioButton = rg.findViewById(selectedId);
            totalScore += Integer.parseInt(radioButton.getTag().toString());
        }

        String level;
        String message;

        // Scoring logic adjusted for 9 questions (max score ~20+)
        if (totalScore <= 4) {
            level = "Low";
            message = "Your responses suggest you're coping well. Continue prioritizing your self-care.";
        } else if (totalScore <= 9) {
            level = "Moderate";
            message = "You're showing some signs of distress. Please talk to a loved one or a doctor soon.";
        } else {
            level = "High";
            message = "Your score indicates significant distress. We strongly recommend contacting a mental health professional or emergency services immediately.";
        }

        tvResult.setText("Assessment Score: " + totalScore + "\nResult: " + level + " Risk\n\n" + message);
        cardResult.setVisibility(View.VISIBLE);

        saveToDb(totalScore, level);
        
        // Scroll to result
        cardResult.getParent().requestChildFocus(cardResult, cardResult);
    }

    private void saveToDb(int score, String level) {
        MoodDbHelper dbHelper = new MoodDbHelper(this);
        ContentValues values = new ContentValues();
        values.put(MoodDbHelper.COLUMN_SCORE, score);
        values.put(MoodDbHelper.COLUMN_RISK_LEVEL, level);
        values.put(MoodDbHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
        dbHelper.insertData(MoodDbHelper.TABLE_ASSESSMENTS, values);
        Toast.makeText(this, "Assessment saved securely.", Toast.LENGTH_SHORT).show();
    }
}
