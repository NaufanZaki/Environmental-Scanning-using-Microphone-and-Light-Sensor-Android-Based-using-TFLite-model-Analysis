package com.example.fpmobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Navigate to ScanActivity
        TextView btnNavigateScan = findViewById(R.id.btnNavigateScan);
        btnNavigateScan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScanActivity.class);
            startActivityForResult(intent, 1); // Request code 1 for scan
        });

        // Navigate to HistoryActivity
        TextView btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Navigate to AnalysisActivity
        TextView btnAnalysis = findViewById(R.id.btnAnalysis);
        btnAnalysis.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AnalysisActivity.class);
            startActivity(intent);
        });

        // Reset UI when the app opens
        resetUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Retrieve scan results
            float scanDecibels = data.getFloatExtra("SCAN_DECIBELS", 0);
            float scanLux = data.getFloatExtra("SCAN_LUX", 0);
            String recommendation = data.getStringExtra("COMFORT_RECOMMENDATION");

            // Example logic to compute comfort level (if not provided by ScanActivity)
            int comfortLevel = (scanDecibels <= 70 && scanLux >= 300) ? 0 :
                    (scanDecibels <= 70) ? 1 :
                            (scanLux >= 300) ? 2 : 3;

            // Update the UI
            updateUI(scanDecibels, scanLux);
            updateRecommendations(recommendation, comfortLevel);
        } else {
            Log.w("MainActivity", "No valid data received from ScanActivity.");
        }
    }

    private void resetUI() {
        TextView decibelTextView = findViewById(R.id.decibels);
        TextView lightLevelTextView = findViewById(R.id.lightLevel);
        TextView suggestionTextView = findViewById(R.id.suggestion);

        decibelTextView.setText("0.00 dB");
        lightLevelTextView.setText("0.00 Lux");
        suggestionTextView.setText("No data available");
        suggestionTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void updateUI(float decibels, float lux) {
        TextView decibelTextView = findViewById(R.id.decibels);
        TextView lightLevelTextView = findViewById(R.id.lightLevel);

        decibelTextView.setText(String.format(Locale.getDefault(), "%.2f dB", decibels));
        lightLevelTextView.setText(String.format(Locale.getDefault(), "%.2f Lux", lux));
    }

    private String getRecommendation(int comfortLevel, float decibels, float lux) {
        switch (comfortLevel) {
            case 1:
                return lux < 300
                        ? "Add a 10-watt LED light for better brightness."
                        : "The environment is good, but consider reducing noise.";
            case 2:
                return decibels > 70
                        ? "Reduce noise with sound absorbers or move to a quieter location."
                        : "Consider additional lighting to improve comfort.";
            case 3:
                return "The environment is very poor for comfort. Improve lighting and reduce noise immediately.";
            default:
                return "The environment is ideal. No action needed.";
        }
    }

    private void updateRecommendations(String recommendation, int comfortLevel) {
        TextView suggestionTextView = findViewById(R.id.suggestion);

        suggestionTextView.setText(recommendation);

        int textColor;
        switch (comfortLevel) {
            case 1: // Good
                textColor = getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case 2: // Poor
                textColor = getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case 3: // Very Poor
                textColor = getResources().getColor(android.R.color.holo_red_dark);
                break;
            default: // Ideal
                textColor = getResources().getColor(android.R.color.holo_green_dark);
                break;
        }

        suggestionTextView.setTextColor(textColor);
    }
}