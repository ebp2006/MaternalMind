package com.example.maternalmindavr.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.maternalmindavr.R;

public class DadsGuideActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dads_guide);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
