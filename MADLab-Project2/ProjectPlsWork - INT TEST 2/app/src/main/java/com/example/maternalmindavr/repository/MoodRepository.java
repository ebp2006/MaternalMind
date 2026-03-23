package com.example.maternalmindavr.repository;

import android.content.Context;
import com.example.maternalmindavr.database.MoodDbHelper;
import com.example.maternalmindavr.models.MoodEntry;
import java.util.List;

/**
 * Repository class to handle data operations.
 * This abstracts the data source from the ViewModel.
 */
public class MoodRepository {
    private final MoodDbHelper dbHelper;

    public MoodRepository(Context context) {
        this.dbHelper = new MoodDbHelper(context);
    }

    public long insertMood(MoodEntry entry) {
        return dbHelper.addMoodEntry(entry);
    }

    public List<MoodEntry> getAllMoods() {
        return dbHelper.getAllMoodEntries();
    }
}
