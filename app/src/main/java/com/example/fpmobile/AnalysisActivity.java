package com.example.fpmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.example.fpmobile.database.ScanDatabase;
import com.example.fpmobile.database.ScanEntity;

import java.util.ArrayList;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // Inisialisasi grafik
        LineChart dBChart = findViewById(R.id.dBChart);
        LineChart lightChart = findViewById(R.id.lightChart);
        Button btnBack = findViewById(R.id.btnBack);

        // Ambil data hasil scan dari database
        new Thread(() -> {
            List<ScanEntity> scanHistory = ScanDatabase.getInstance(this).scanDao().getAllScans();

            // Perbarui grafik di UI thread
            runOnUiThread(() -> {
                setupDecibelChart(dBChart, scanHistory);
                setupLuxChart(lightChart, scanHistory);
            });
        }).start();

        // Tombol kembali ke halaman utama
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AnalysisActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupDecibelChart(LineChart lineChart, List<ScanEntity> scanHistory) {
        // Set MarkerView
        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.marker_view);
        lineChart.setMarker(markerView);

        ArrayList<Entry> decibelEntries = new ArrayList<>();

        // Add decibel data to the chart
        for (int i = 0; i < scanHistory.size(); i++) {
            ScanEntity scan = scanHistory.get(i);
            decibelEntries.add(new Entry(i, scan.getDecibels()));
        }

        // Customize the dataset
        LineDataSet decibelDataSet = new LineDataSet(decibelEntries, "Decibels");
        decibelDataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        decibelDataSet.setCircleColor(getResources().getColor(android.R.color.holo_blue_dark));
        decibelDataSet.setCircleRadius(5f);
        decibelDataSet.setCircleHoleRadius(2.5f);
        decibelDataSet.setDrawValues(true);
        decibelDataSet.setValueTextSize(12f);
        decibelDataSet.setValueTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        decibelDataSet.setDrawFilled(true);
        decibelDataSet.setFillColor(getResources().getColor(android.R.color.holo_blue_light));
        decibelDataSet.setLineWidth(3f);

        // Set data and animate the chart
        LineData lineData = new LineData(decibelDataSet);
        lineChart.setData(lineData);
        lineChart.animateX(1500);
        lineChart.animateY(1500);

        // Configure chart appearance
        configureChartAppearance(lineChart);

        // Refresh the chart view
        lineChart.invalidate();
    }


    private void setupLuxChart(LineChart lineChart, List<ScanEntity> scanHistory) {
        ArrayList<Entry> luxEntries = new ArrayList<>();

        // Set MarkerView
        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.marker_view);
        lineChart.setMarker(markerView);

        // Add Lux data to the chart
        for (int i = 0; i < scanHistory.size(); i++) {
            ScanEntity scan = scanHistory.get(i);
            luxEntries.add(new Entry(i, scan.getLux()));
        }

        // Customize the dataset
        LineDataSet luxDataSet = new LineDataSet(luxEntries, "Lux");
        luxDataSet.setColor(getResources().getColor(android.R.color.holo_orange_dark));
        luxDataSet.setCircleColor(getResources().getColor(android.R.color.holo_orange_dark));
        luxDataSet.setCircleRadius(5f);
        luxDataSet.setCircleHoleRadius(2.5f);
        luxDataSet.setDrawValues(true);
        luxDataSet.setValueTextSize(12f);
        luxDataSet.setValueTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        luxDataSet.setDrawFilled(true);
        luxDataSet.setFillColor(getResources().getColor(android.R.color.holo_orange_light));
        luxDataSet.setLineWidth(3f);

        // Set data and animate the chart
        LineData lineData = new LineData(luxDataSet);
        lineChart.setData(lineData);
        lineChart.animateX(1500);
        lineChart.animateY(1500);

        // Configure chart appearance
        configureChartAppearance(lineChart);

        // Refresh the chart view
        lineChart.invalidate();
    }


    private void configureChartAppearance(LineChart lineChart) {
        // Atur sumbu X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        // Atur sumbu Y
        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Nonaktifkan sumbu kanan

        // Pengaturan umum
        lineChart.getDescription().setEnabled(false); // Hilangkan deskripsi
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.invalidate(); // Refresh grafik
    }
}
