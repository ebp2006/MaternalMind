package com.example.maternalmindavr.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.maternalmindavr.models.MoodEntry;
import java.util.ArrayList;
import java.util.List;

public class MoodDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "maternal_mind_v2.db";
    private static final int DATABASE_VERSION = 3; // Incremented version for heart rate

    // Mood Table
    public static final String TABLE_MOODS = "mood_logs";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MOOD = "mood";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Passive Data Table
    public static final String TABLE_PASSIVE = "passive_data";
    public static final String COLUMN_SLEEP = "sleep_hours";
    public static final String COLUMN_ACTIVITY = "activity_level";
    public static final String COLUMN_SCREEN_TIME = "screen_time";
    public static final String COLUMN_HEART_RATE = "heart_rate"; // New column

    // Assessment Table
    public static final String TABLE_ASSESSMENTS = "assessment_scores";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_RISK_LEVEL = "risk_level";

    // Cognitive Test Table
    public static final String TABLE_COGNITIVE = "cognitive_tests";
    public static final String COLUMN_TEST_NAME = "test_name";
    public static final String COLUMN_TEST_SCORE = "test_score";

    private static final String CREATE_MOOD_TABLE =
            "CREATE TABLE " + TABLE_MOODS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_MOOD + " TEXT, " +
            COLUMN_NOTE + " TEXT, " +
            COLUMN_TIMESTAMP + " INTEGER);";

    private static final String CREATE_PASSIVE_TABLE =
            "CREATE TABLE " + TABLE_PASSIVE + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SLEEP + " REAL, " +
            COLUMN_ACTIVITY + " TEXT, " +
            COLUMN_SCREEN_TIME + " REAL, " +
            COLUMN_HEART_RATE + " REAL, " +
            COLUMN_TIMESTAMP + " INTEGER);";

    private static final String CREATE_ASSESSMENT_TABLE =
            "CREATE TABLE " + TABLE_ASSESSMENTS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SCORE + " INTEGER, " +
            COLUMN_RISK_LEVEL + " TEXT, " +
            COLUMN_TIMESTAMP + " INTEGER);";

    private static final String CREATE_COGNITIVE_TABLE =
            "CREATE TABLE " + TABLE_COGNITIVE + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TEST_NAME + " TEXT, " +
            COLUMN_TEST_SCORE + " INTEGER, " +
            COLUMN_TIMESTAMP + " INTEGER);";

    public MoodDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MOOD_TABLE);
        db.execSQL(CREATE_PASSIVE_TABLE);
        db.execSQL(CREATE_ASSESSMENT_TABLE);
        db.execSQL(CREATE_COGNITIVE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_PASSIVE + " ADD COLUMN " + COLUMN_HEART_RATE + " REAL DEFAULT 0");
        }
    }

    // Generic Insert Method
    public long insertData(String table, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(table, null, values);
        db.close();
        return id;
    }

    // Existing mood method for compatibility or migration
    public long addMoodEntry(MoodEntry entry) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MOOD, entry.getMood());
        values.put(COLUMN_NOTE, entry.getNote());
        values.put(COLUMN_TIMESTAMP, entry.getTimestamp());
        return insertData(TABLE_MOODS, values);
    }

    public List<MoodEntry> getAllMoodEntries() {
        List<MoodEntry> entries = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MOODS + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                MoodEntry entry = new MoodEntry();
                entry.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                entry.setMood(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD)));
                entry.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE)));
                entry.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }
}
