package com.example.maternalmindavr.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.maternalmindavr.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class PrivacyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        MaterialSwitch dataSwitch = findViewById(R.id.switchDataCollection);
        dataSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Passive data collection enabled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Passive data collection disabled.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
