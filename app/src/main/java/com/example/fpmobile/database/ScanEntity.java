package com.example.fpmobile.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_history")
public class ScanEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private float decibels;
    private float lux;
    private String recommendation;
    private long timestamp;
    private String photoPath;  // New field to store the photo file path

    public ScanEntity(float decibels, float lux, String recommendation, long timestamp, String photoPath) {
        this.decibels = decibels;
        this.lux = lux;
        this.recommendation = recommendation;
        this.timestamp = timestamp;
        this.photoPath = photoPath;
    }

    // Add getter and setter for photoPath
    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    // Existing getters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getDecibels() {
        return decibels;
    }

    public float getLux() {
        return lux;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public long getTimestamp() {
        return timestamp;
    }
}