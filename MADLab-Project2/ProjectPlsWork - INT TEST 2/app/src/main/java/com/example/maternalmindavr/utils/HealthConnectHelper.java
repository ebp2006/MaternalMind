package com.example.maternalmindavr.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.permission.HealthPermission;
import androidx.health.connect.client.records.BloodPressureRecord;
import androidx.health.connect.client.records.HeartRateRecord;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.request.ReadRecordsRequest;
import androidx.health.connect.client.response.ReadRecordsResponse;
import androidx.health.connect.client.time.TimeRangeFilter;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.internal.Reflection;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;

public class HealthConnectHelper {
    private static final String TAG = "HealthConnectHelper";

    public static final Set<String> PERMISSIONS = new HashSet<>(Arrays.asList(
            HealthPermission.getReadPermission(Reflection.getOrCreateKotlinClass(HeartRateRecord.class)),
            HealthPermission.getReadPermission(Reflection.getOrCreateKotlinClass(StepsRecord.class)),
            HealthPermission.getReadPermission(Reflection.getOrCreateKotlinClass(BloodPressureRecord.class))
    ));

    public static boolean isHealthConnectAvailable(Context context) {
        try {
            int availabilityStatus = HealthConnectClient.getSdkStatus(context);
            return availabilityStatus == HealthConnectClient.SDK_AVAILABLE;
        } catch (Exception e) {
            Log.e(TAG, "Error checking Health Connect availability", e);
            return false;
        }
    }

    public static void promptInstallHealthConnect(Context context) {
        String url = "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setPackage("com.android.vending");
        context.startActivity(intent);
    }

    public static CompletableFuture<Set<String>> getGrantedPermissions(HealthConnectClient client) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return (Set<String>) BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
                    client.getPermissionController().getGrantedPermissions(continuation)
                );
            } catch (Exception e) {
                Log.e(TAG, "Error getting granted permissions", e);
                return Collections.emptySet();
            }
        });
    }

    public static CompletableFuture<ReadRecordsResponse<HeartRateRecord>> readHeartRate(HealthConnectClient client, Instant start, Instant end) {
        return CompletableFuture.supplyAsync(() -> {
            ReadRecordsRequest<HeartRateRecord> request = new ReadRecordsRequest<>(
                    Reflection.getOrCreateKotlinClass(HeartRateRecord.class),
                    TimeRangeFilter.between(start, end),
                    Collections.emptySet(),
                    false,
                    10,
                    null
            );
            try {
                return (ReadRecordsResponse<HeartRateRecord>) BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
                    client.readRecords(request, continuation)
                );
            } catch (Exception e) {
                Log.e(TAG, "Error reading heart rate records", e);
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<ReadRecordsResponse<BloodPressureRecord>> readBloodPressure(HealthConnectClient client, Instant start, Instant end) {
        return CompletableFuture.supplyAsync(() -> {
            ReadRecordsRequest<BloodPressureRecord> request = new ReadRecordsRequest<>(
                    Reflection.getOrCreateKotlinClass(BloodPressureRecord.class),
                    TimeRangeFilter.between(start, end),
                    Collections.emptySet(),
                    false,
                    10,
                    null
            );
            try {
                return (ReadRecordsResponse<BloodPressureRecord>) BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
                    client.readRecords(request, continuation)
                );
            } catch (Exception e) {
                Log.e(TAG, "Error reading blood pressure records", e);
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<AggregationResult> readSteps(HealthConnectClient client, Instant start, Instant end) {
        return CompletableFuture.supplyAsync(() -> {
            Set<AggregateMetric<Long>> metrics = new HashSet<>();
            metrics.add(StepsRecord.COUNT_TOTAL);
            
            AggregateRequest request = new AggregateRequest(
                    metrics,
                    TimeRangeFilter.between(start, end),
                    Collections.emptySet()
            );
            try {
                return (AggregationResult) BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
                    client.aggregate(request, continuation)
                );
            } catch (Exception e) {
                Log.e(TAG, "Error reading steps aggregation", e);
                throw new RuntimeException(e);
            }
        });
    }
}
