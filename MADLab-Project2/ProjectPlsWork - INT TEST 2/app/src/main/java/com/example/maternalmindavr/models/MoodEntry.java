package com.example.maternalmindavr.models;

public class MoodEntry {
    private int id;
    private String mood;
    private String note;
    private long timestamp;

    public MoodEntry() {}

    public MoodEntry(int id, String mood, String note, long timestamp) {
        this.id = id;
        this.mood = mood;
        this.note = note;
        this.timestamp = timestamp;
    }

    public MoodEntry(String mood, String note, long timestamp) {
        this.mood = mood;
        this.note = note;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
