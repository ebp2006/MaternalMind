package com.example.maternalmindavr;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.PermissionController;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.BloodPressureRecord;
import androidx.health.connect.client.records.HeartRateRecord;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.response.ReadRecordsResponse;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.maternalmindavr.activities.AssessmentActivity;
import com.example.maternalmindavr.activities.CognitiveTestActivity;
import com.example.maternalmindavr.activities.DadsGuideActivity;
import com.example.maternalmindavr.activities.HelpActivity;
import com.example.maternalmindavr.activities.InsightsActivity;
import com.example.maternalmindavr.activities.MoodActivity;
import com.example.maternalmindavr.activities.PrivacyActivity;
import com.example.maternalmindavr.database.MoodDbHelper;
import com.example.maternalmindavr.ml.MLModelManager;
import com.example.maternalmindavr.models.MoodEntry;
import com.example.maternalmindavr.utils.BackgroundSyncWorker;
import com.example.maternalmindavr.utils.HealthConnectHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String SYNC_WORK_NAME = "MaternalMindSyncWork";

    private TextView tvHeartRate;
    private TextView tvHeartRateStatus;
    private TextView tvBPValue;
    private TextView tvBPStatus;
    private TextView tvStepsValue;
    private TextView tvQuoteText;
    private MoodDbHelper dbHelper;
    private MLModelManager mlManager;
    private HealthConnectClient healthConnectClient;
    
    private boolean isRequestingPermissions = false;

    private final ActivityResultLauncher<Set<String>> requestPermissionLauncher =
            registerForActivityResult(PermissionController.createRequestPermissionResultContract(), granted -> {
                isRequestingPermissions = false;
                if (granted != null && granted.containsAll(HealthConnectHelper.PERMISSIONS)) {
                    Log.d(TAG, "Permissions granted by user");
                    fetchAllHealthData();
                } else {
                    Log.w(TAG, "Permissions denied by user");
                    Toast.makeText(this, "Permissions denied. Using simulated data.", Toast.LENGTH_SHORT).show();
                    simulateAllData();
                }
            });

    private final String[] lowRiskQuotes = {
        "You are doing amazing. Keep breathing and taking care of yourself.",
        "Your peace is a priority. Great job maintaining your routine today!",
        "Every small step counts. You're handling motherhood beautifully.",
        "Believe in yourself. You are the exact mother your baby needs."
    };

    private final String[] moderateRiskQuotes = {
        "It's okay to feel overwhelmed. Remember, you don't have to do this alone.",
        "Take a deep breath. This moment is tough, but so are you.",
        "Self-care is not selfish. Take 5 minutes just for you right now.",
        "Your feelings are valid. Reach out to a friend for a quick chat today."
    };

    private final String[] highRiskQuotes = {
        "Please take a moment for yourself. Your wellness is the priority right now.",
        "You've been carrying a lot. It's time to let someone help you carry the load.",
        "Your health is the foundation. Please prioritize rest and reaching out today.",
        "It is okay to not be okay. Help is available and you deserve support."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        dbHelper = new MoodDbHelper(this);
        mlManager = new MLModelManager();

        tvHeartRate = findViewById(R.id.tvHeartRateValue);
        tvHeartRateStatus = findViewById(R.id.tvHeartRateStatus);
        tvBPValue = findViewById(R.id.tvBPValue);
        tvBPStatus = findViewById(R.id.tvBPStatus);
        tvStepsValue = findViewById(R.id.tvStepsValue);
        tvQuoteText = findViewById(R.id.tvQuoteText);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupHealthConnect();
        setupNavigation();
        scheduleBackgroundWork();
        setupDashboardCards();
        
        findViewById(R.id.btnRefreshHeartRate).setOnClickListener(v -> manualRefresh());
        findViewById(R.id.btnRefreshBP).setOnClickListener(v -> manualRefresh());
        findViewById(R.id.btnRefreshSteps).setOnClickListener(v -> fetchStepsFromHealthConnect());
        findViewById(R.id.btnSimulateAbnormal).setOnClickListener(v -> simulateAbnormalHeartRate());
        
        findViewById(R.id.btnDadsGuide).setOnClickListener(v -> startActivity(new Intent(this, DadsGuideActivity.class)));
        
        refreshData(false);
    }

    private void setupHealthConnect() {
        if (HealthConnectHelper.isHealthConnectAvailable(this)) {
            healthConnectClient = HealthConnectClient.getOrCreate(this);
            Log.d(TAG, "Health Connect Client initialized");
        } else {
            Log.w(TAG, "Health Connect is NOT available on this device");
        }
    }

    private void manualRefresh() {
        Log.d(TAG, "Manual refresh triggered");
        refreshData(true);
    }

    private void refreshData(boolean forcePermissionRequest) {
        if (HealthConnectHelper.isHealthConnectAvailable(this)) {
            checkHealthConnectPermissionsAndFetch(forcePermissionRequest);
        } else {
            Log.d(TAG, "Falling back to simulation: Health Connect unavailable");
            simulateAllData();
        }
    }

    private void checkHealthConnectPermissionsAndFetch(boolean forceRequest) {
        if (healthConnectClient == null) {
            setupHealthConnect();
            if (healthConnectClient == null) {
                simulateAllData();
                return;
            }
        }
        
        HealthConnectHelper.getGrantedPermissions(healthConnectClient).thenAccept(grantedPermissions -> {
            Log.d(TAG, "Granted permissions: " + grantedPermissions);
            
            if (grantedPermissions.containsAll(HealthConnectHelper.PERMISSIONS)) {
                Log.d(TAG, "All permissions granted. Fetching data...");
                fetchAllHealthData();
            } else if (forceRequest && !isRequestingPermissions) {
                Log.d(TAG, "Permissions missing. Launching permission request...");
                isRequestingPermissions = true;
                runOnUiThread(() -> requestPermissionLauncher.launch(HealthConnectHelper.PERMISSIONS));
            } else {
                Log.d(TAG, "Permissions missing. Using simulation");
                runOnUiThread(() -> simulateAllData());
            }
        }).exceptionally(t -> {
            Log.e(TAG, "Error checking permissions", t);
            runOnUiThread(() -> simulateAllData());
            return null;
        });
    }

    private void fetchAllHealthData() {
        fetchHeartRateFromHealthConnect();
        fetchBloodPressureFromHealthConnect();
        fetchStepsFromHealthConnect();
    }

    private void simulateAllData() {
        simulateHeartRate();
        simulateBP();
        simulateSteps();
    }

    private void simulateAbnormalHeartRate() {
        Log.d(TAG, "Simulating abnormal heart rate for alert test");
        // Force an abnormal BPM and bypass the 'isSimulated' check for alert display
        processHeartRateData(125, false); 
        Toast.makeText(this, "Simulating High Heart Rate (125 BPM)", Toast.LENGTH_SHORT).show();
    }

    private void fetchHeartRateFromHealthConnect() {
        if (healthConnectClient == null) return;

        Instant end = Instant.now();
        Instant start = end.minus(90, ChronoUnit.DAYS);
        
        HealthConnectHelper.readHeartRate(healthConnectClient, start, end).thenAccept(response -> {
            List<HeartRateRecord> records = response.getRecords();
            if (!records.isEmpty()) {
                HeartRateRecord latestRecord = records.get(records.size() - 1);
                if (!latestRecord.getSamples().isEmpty()) {
                    long bpm = latestRecord.getSamples().get(latestRecord.getSamples().size() - 1).getBeatsPerMinute();
                    runOnUiThread(() -> processHeartRateData(bpm, false));
                } else {
                    runOnUiThread(() -> simulateHeartRate());
                }
            } else {
                runOnUiThread(() -> simulateHeartRate());
            }
        }).exceptionally(t -> {
            Log.e(TAG, "Error fetching heart rate", t);
            runOnUiThread(() -> simulateHeartRate());
            return null;
        });
    }

    private void fetchBloodPressureFromHealthConnect() {
        if (healthConnectClient == null) return;

        Instant end = Instant.now();
        Instant start = end.minus(90, ChronoUnit.DAYS);
        
        HealthConnectHelper.readBloodPressure(healthConnectClient, start, end).thenAccept(response -> {
            List<BloodPressureRecord> records = response.getRecords();
            if (!records.isEmpty()) {
                BloodPressureRecord latestRecord = records.get(records.size() - 1);
                int systolic = (int) latestRecord.getSystolic().getMillimetersOfMercury();
                int diastolic = (int) latestRecord.getDiastolic().getMillimetersOfMercury();
                runOnUiThread(() -> processBloodPressureData(systolic, diastolic, false));
            } else {
                runOnUiThread(() -> simulateBP());
            }
        }).exceptionally(t -> {
            Log.e(TAG, "Error fetching BP", t);
            runOnUiThread(() -> simulateBP());
            return null;
        });
    }

    private void fetchStepsFromHealthConnect() {
        if (healthConnectClient == null) {
            setupHealthConnect();
            if (healthConnectClient == null) {
                simulateSteps();
                return;
            }
        }

        HealthConnectHelper.getGrantedPermissions(healthConnectClient).thenAccept(perms -> {
            if (perms.contains(HealthConnectHelper.PERMISSIONS.iterator().next())) { // Simple check for steps permission
                Instant start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant end = Instant.now();
                
                HealthConnectHelper.readSteps(healthConnectClient, start, end).thenAccept(result -> {
                    Long steps = result.get(StepsRecord.COUNT_TOTAL);
                    runOnUiThread(() -> processStepsData(steps != null ? steps : 0, false));
                    if (steps != null) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Steps synced: " + steps, Toast.LENGTH_SHORT).show());
                    }
                }).exceptionally(t -> {
                    Log.e(TAG, "Error fetching steps", t);
                    runOnUiThread(() -> simulateSteps());
                    return null;
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Steps permission required to sync", Toast.LENGTH_SHORT).show();
                    simulateSteps();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(false);
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_insights) {
                startActivity(new Intent(this, InsightsActivity.class));
                return true;
            }
            if (id == R.id.nav_assessment) {
                startActivity(new Intent(this, AssessmentActivity.class));
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, PrivacyActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupDashboardCards() {
        findViewById(R.id.cardMood).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MoodActivity.class)));
        findViewById(R.id.cardHelp).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpActivity.class)));
        findViewById(R.id.cardCognitive).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CognitiveTestActivity.class)));
        
        findViewById(R.id.cardMoodBooster).setOnClickListener(v -> {
            String url = "https://www.youtube.com/results?search_query=cute+baby+animals+and+funny+videos";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
        
        View assessmentCard = findViewById(R.id.cardEPDSAssessment);
        if (assessmentCard != null) {
            assessmentCard.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AssessmentActivity.class)));
        }
    }

    private void scheduleBackgroundWork() {
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(BackgroundSyncWorker.class, 24, TimeUnit.HOURS).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, syncRequest);
    }

    private void simulateHeartRate() {
        Random random = new Random();
        float heartRate = 65 + random.nextInt(30); 
        processHeartRateData((long)heartRate, true);
    }

    private void simulateBP() {
        Random random = new Random();
        int systolic = 110 + random.nextInt(20); 
        int diastolic = 70 + random.nextInt(15);
        processBloodPressureData(systolic, diastolic, true);
    }

    private void simulateSteps() {
        Random random = new Random();
        long steps = 1000 + random.nextInt(5000);
        processStepsData(steps, true);
    }

    private void processHeartRateData(long heartRate, boolean isSimulated) {
        tvHeartRate.setText(heartRate + " BPM" + (isSimulated ? " (Simulated)" : ""));
        
        int color;
        String status;
        if (heartRate < 60) {
            color = ContextCompat.getColor(this, R.color.status_abnormal);
            status = "Status: Low";
            if (!isSimulated) showEmergencyAlert("Heart Rate (" + heartRate + " BPM)");
        } else if (heartRate > 100) {
            color = ContextCompat.getColor(this, R.color.status_abnormal);
            status = "Status: High";
            if (!isSimulated) showEmergencyAlert("Heart Rate (" + heartRate + " BPM)");
        } else {
            color = ContextCompat.getColor(this, R.color.status_normal);
            status = "Status: Normal";
        }

        tvHeartRateStatus.setText(status);
        tvHeartRateStatus.setTextColor(color);
        tvHeartRateStatus.setVisibility(View.VISIBLE);
        tvHeartRate.setTextColor(color);
        
        saveHeartRateToDb((float)heartRate);
        performRiskAnalysis();
    }

    private void processBloodPressureData(int systolic, int diastolic, boolean isSimulated) {
        tvBPValue.setText(systolic + "/" + diastolic + " mmHg" + (isSimulated ? " (Simulated)" : ""));
        
        int color;
        boolean isAbnormal = false;
        String bpStatus = "";

        if (systolic >= 140 || diastolic >= 90) {
            bpStatus = "Status: High (Hypertension)";
            color = ContextCompat.getColor(this, R.color.status_abnormal);
            isAbnormal = true;
        } else if (systolic < 90 || diastolic < 60) {
            bpStatus = "Status: Low (Hypotension)";
            color = ContextCompat.getColor(this, R.color.status_abnormal);
            isAbnormal = true;
        } else {
            bpStatus = "Status: Normal";
            color = ContextCompat.getColor(this, R.color.status_normal);
        }

        tvBPStatus.setText(bpStatus);
        tvBPStatus.setTextColor(color);
        tvBPStatus.setVisibility(View.VISIBLE);
        tvBPValue.setTextColor(color);

        if (isAbnormal && !isSimulated) {
            showEmergencyAlert("Blood Pressure (" + systolic + "/" + diastolic + ")");
        }
        performRiskAnalysis();
    }

    private void processStepsData(long steps, boolean isSimulated) {
        tvStepsValue.setText(steps + (isSimulated ? " (Simulated)" : ""));
    }

    private void saveHeartRateToDb(float heartRate) {
        ContentValues values = new ContentValues();
        values.put(MoodDbHelper.COLUMN_HEART_RATE, heartRate);
        values.put(MoodDbHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
        dbHelper.insertData(MoodDbHelper.TABLE_PASSIVE, values);
    }

    private void performRiskAnalysis() {
        List<MoodEntry> entries = dbHelper.getAllMoodEntries();
        MLModelManager.RiskResult result = mlManager.predictRisk(this, entries);
        updateMotivationalQuote(result.level);
    }

    private void updateMotivationalQuote(String riskLevel) {
        Random random = new Random();
        String quote;
        switch (riskLevel) {
            case "Low": quote = lowRiskQuotes[random.nextInt(lowRiskQuotes.length)]; break;
            case "Moderate": quote = moderateRiskQuotes[random.nextInt(moderateRiskQuotes.length)]; break;
            case "High": quote = highRiskQuotes[random.nextInt(highRiskQuotes.length)]; break;
            default: quote = "You are stronger than you know. One step at a time.";
        }
        tvQuoteText.setText(quote);
    }

    private void showEmergencyAlert(String vitalName) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Health Alert: " + vitalName)
                .setMessage("Your " + vitalName + " reading is outside the normal range. " +
                        "This could be a sign of postpartum complications like Preeclampsia or severe exhaustion.\n\n" +
                        "Emergency Contacts:\n" +
                        "• Ambulance: 102\n" +
                        "• Hospital Helpline: 112\n" +
                        "• Women Helpline: 181\n\n" +
                        "Please contact your doctor or go to the nearest emergency room immediately.")
                .setPositiveButton("Call Emergency", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:112"));
                    startActivity(intent);
                })
                .setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
