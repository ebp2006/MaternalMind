package com.example.maternalmindavr.activities;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.maternalmindavr.R;
import com.example.maternalmindavr.database.MoodDbHelper;
import java.util.Random;

public class CognitiveTestActivity extends AppCompatActivity {
    private Button btnTap;
    private TextView tvReaction;
    private long startTime;
    private boolean isWaiting = true;
    private boolean testFinished = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cognitive);

        btnTap = findViewById(R.id.btnTapTest);
        tvReaction = findViewById(R.id.tvReactionTime);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        startTestCycle();

        btnTap.setOnClickListener(v -> {
            if (testFinished) {
                startTestCycle();
            } else if (isWaiting) {
                handler.removeCallbacks(runnable);
                Toast.makeText(this, "Too early! Wait for Pink.", Toast.LENGTH_SHORT).show();
                startTestCycle();
            } else {
                long endTime = System.currentTimeMillis();
                long reactionTime = endTime - startTime;
                tvReaction.setText("Reaction Time: " + reactionTime + " ms");
                tvReaction.setVisibility(View.VISIBLE);
                saveScore((int) reactionTime);
                
                btnTap.setText("RESTART");
                btnTap.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary_lavender));
                testFinished = true;
                isWaiting = false;
            }
        });
    }

    private void startTestCycle() {
        testFinished = false;
        isWaiting = true;
        btnTap.setText("WAIT...");
        btnTap.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary_lavender));
        tvReaction.setVisibility(View.GONE);

        int delay = new Random().nextInt(3000) + 2000; // 2-5 seconds
        runnable = () -> {
            btnTap.setText("TAP NOW!");
            btnTap.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_pink));
            startTime = System.currentTimeMillis();
            isWaiting = false;
        };
        handler.postDelayed(runnable, delay);
    }

    private void saveScore(int score) {
        MoodDbHelper dbHelper = new MoodDbHelper(this);
        ContentValues values = new ContentValues();
        values.put(MoodDbHelper.COLUMN_TEST_NAME, "Reaction Tap");
        values.put(MoodDbHelper.COLUMN_TEST_SCORE, score);
        values.put(MoodDbHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
        dbHelper.insertData(MoodDbHelper.TABLE_COGNITIVE, values);
        Toast.makeText(this, "Score saved locally.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent memory leaks by removing callbacks when activity is destroyed
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
