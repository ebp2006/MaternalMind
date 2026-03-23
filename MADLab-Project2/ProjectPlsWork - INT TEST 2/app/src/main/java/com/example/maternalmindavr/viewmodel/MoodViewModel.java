package com.example.maternalmindavr.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.maternalmindavr.models.MoodEntry;
import com.example.maternalmindavr.repository.MoodRepository;
import java.util.List;

public class MoodViewModel extends AndroidViewModel {
    private final MoodRepository repository;
    private final MutableLiveData<List<MoodEntry>> allMoods = new MutableLiveData<>();

    public MoodViewModel(@NonNull Application application) {
        super(application);
        repository = new MoodRepository(application);
        loadAllMoods();
    }

    public LiveData<List<MoodEntry>> getAllMoods() {
        return allMoods;
    }

    public void loadAllMoods() {
        allMoods.setValue(repository.getAllMoods());
    }

    public void addMood(MoodEntry entry) {
        repository.insertMood(entry);
        loadAllMoods();
    }
}
