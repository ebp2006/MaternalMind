package com.example.maternalmindavr.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.maternalmindavr.R;
import com.example.maternalmindavr.adapters.MoodAdapter;
import com.example.maternalmindavr.database.MoodDbHelper;
import com.example.maternalmindavr.ml.MLModelManager;
import com.example.maternalmindavr.models.MoodEntry;
import java.util.List;

public class InsightsActivity extends AppCompatActivity {
    private RecyclerView rvHistory;
    private TextView tvRiskLevel, tvRecommendation, tvSleepTrend, tvActivityTrend;
    private MoodDbHelper dbHelper;
    private MLModelManager mlManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        dbHelper = new MoodDbHelper(this);
        mlManager = new MLModelManager();
        
        rvHistory = findViewById(R.id.rvMoodHistory);
        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvRecommendation = findViewById(R.id.tvRecommendation);
        tvSleepTrend = findViewById(R.id.tvSleepTrend);
        tvActivityTrend = findViewById(R.id.tvActivityTrend);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        
        loadInsights();
    }

    private void loadInsights() {
        List<MoodEntry> entries = dbHelper.getAllMoodEntries();
        MoodAdapter adapter = new MoodAdapter(entries);
        rvHistory.setAdapter(adapter);

        // ML Risk Prediction
        MLModelManager.RiskResult result = mlManager.predictRisk(this, entries);
        tvRiskLevel.setText("Risk Level: " + result.level);
        tvRecommendation.setText(result.advice);

        // Behavioral Trends (Simulated from Passive Data table)
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(" + MoodDbHelper.COLUMN_SLEEP + ") as avg_sleep FROM " + MoodDbHelper.TABLE_PASSIVE, null);
        if (cursor.moveToFirst()) {
            float avgSleep = cursor.getFloat(0);
            if (avgSleep > 0) {
                tvSleepTrend.setText(String.format("Sleep: %.1fh (Avg)", avgSleep));
            }
        }
        cursor.close();

        Cursor actCursor = db.rawQuery("SELECT " + MoodDbHelper.COLUMN_ACTIVITY + " FROM " + MoodDbHelper.TABLE_PASSIVE + " ORDER BY " + MoodDbHelper.COLUMN_TIMESTAMP + " DESC LIMIT 1", null);
        if (actCursor.moveToFirst()) {
            String lastAct = actCursor.getString(0);
            tvActivityTrend.setText("Last Activity: " + lastAct);
        }
        actCursor.close();
    }
}
