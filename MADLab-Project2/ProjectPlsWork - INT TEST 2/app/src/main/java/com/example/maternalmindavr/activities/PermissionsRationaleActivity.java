package com.example.maternalmindavr.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.maternalmindavr.R;

/**
 * Required by Health Connect to explain why permissions are needed.
 */
public class PermissionsRationaleActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // You can add a layout with text explaining the permissions here
        // For now, we'll just finish to return to the permission request
        finish();
    }
}
