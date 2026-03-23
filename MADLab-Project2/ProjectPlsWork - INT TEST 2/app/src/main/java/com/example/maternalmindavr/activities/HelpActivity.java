package com.example.maternalmindavr.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.maternalmindavr.R;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCall1).setOnClickListener(v -> makeCall("9152987821"));
        findViewById(R.id.btnCall2).setOnClickListener(v -> makeCall("181"));
        findViewById(R.id.btnCall3).setOnClickListener(v -> makeCall("112"));
    }

    private void makeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }
}
