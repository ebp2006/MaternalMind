package com.example.maternalmindavr.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.maternalmindavr.R;
import com.example.maternalmindavr.database.MoodDbHelper;
import com.example.maternalmindavr.models.MoodEntry;

public class MoodActivity extends AppCompatActivity {
    private RadioGroup rgMood;
    private EditText etNote;
    private Button btnSave;
    private ImageButton btnBack;
    private MoodDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        dbHelper = new MoodDbHelper(this);
        rgMood = findViewById(R.id.rgMood);
        etNote = findViewById(R.id.etNote);
        btnSave = findViewById(R.id.btnSaveMood);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveMood());
    }

    private void saveMood() {
        int selectedId = rgMood.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedButton = findViewById(selectedId);
        String mood = selectedButton.getText().toString();
        String note = etNote.getText().toString();
        long timestamp = System.currentTimeMillis();

        MoodEntry entry = new MoodEntry(mood, note, timestamp);
        long id = dbHelper.addMoodEntry(entry);

        if (id != -1) {
            Toast.makeText(this, "Mood saved! Take care of yourself.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving mood", Toast.LENGTH_SHORT).show();
        }
    }
}
