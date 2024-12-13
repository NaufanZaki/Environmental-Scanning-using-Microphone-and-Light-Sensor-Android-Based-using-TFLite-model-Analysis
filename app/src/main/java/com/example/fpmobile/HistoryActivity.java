package com.example.fpmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fpmobile.database.ScanDatabase;
import com.example.fpmobile.database.ScanEntity;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize RecyclerView
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Navigate to scan page
        TextView btnNavigateScan = findViewById(R.id.btnNavigateScan);
        btnNavigateScan.setOnClickListener(v -> {
            startActivity(new Intent(HistoryActivity.this, ScanActivity.class));
        });

        // Button to navigate to Home
        TextView btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(HistoryActivity.this, MainActivity.class));
        });

        // Button to navigate to Analysis page
        TextView btnAnalysis = findViewById(R.id.btnAnalysis);
        btnAnalysis.setOnClickListener(v -> {
            startActivity(new Intent(HistoryActivity.this, AnalysisActivity.class));
        });

        // Fetch data from database and bind to RecyclerView
        new Thread(() -> {
            List<ScanEntity> scans = ScanDatabase.getInstance(this).scanDao().getAllScans();

            runOnUiThread(() -> {
                if (scans == null || scans.isEmpty()) {
                    findViewById(R.id.titleTextView).setVisibility(View.VISIBLE); // Show "No history available"
                } else {
                    historyAdapter = new HistoryAdapter(scans);
                    historyRecyclerView.setAdapter(historyAdapter);
                }
            });

        }).start();
    }
}
