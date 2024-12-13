package com.example.fpmobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fpmobile.database.ScanDatabase;
import com.example.fpmobile.database.ScanEntity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ScanDetailActivity extends AppCompatActivity {

    private TextView decibelsTextView, luxTextView, recommendationTextView, timestampTextView;
    private ImageView roomPhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_detail);

        // Tombol untuk membuka riwayat
        Button BackButton = findViewById(R.id.BackButton);
        BackButton.setOnClickListener(v -> {
            Intent intent = new Intent(ScanDetailActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Inisialisasi Views
        decibelsTextView = findViewById(R.id.decibelsTextView);
        luxTextView = findViewById(R.id.luxTextView);
        recommendationTextView = findViewById(R.id.recommendationTextView);
        timestampTextView = findViewById(R.id.timestampTextView);
        roomPhotoImageView = findViewById(R.id.roomPhotoImageView);

        // Ambil ID scan dari Intent
        int scanId = getIntent().getIntExtra("scanId", -1);

        // Validasi scanId
        if (scanId == -1) {
            Toast.makeText(this, "Invalid Scan ID", Toast.LENGTH_SHORT).show();
            finish(); // Kembali ke aktivitas sebelumnya jika ID tidak valid
            return;
        }

        // Fetch data dari database berdasarkan scanId
        new Thread(() -> {
            ScanEntity scan = ScanDatabase.getInstance(this).scanDao().getScanById(scanId);

            // Perbarui UI di thread utama
            runOnUiThread(() -> {
                if (scan != null) {
                    // Format timestamp
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String formattedTimestamp = sdf.format(scan.getTimestamp());

                    // Tampilkan data
                    decibelsTextView.setText(String.format("Decibels: %.2f dB", scan.getDecibels()));
                    luxTextView.setText(String.format("Lux: %.2f", scan.getLux()));
                    recommendationTextView.setText("Recommendation: " + scan.getRecommendation());
                    timestampTextView.setText("Time: " + formattedTimestamp);
                } else {
                    // Jika data tidak ditemukan
                    decibelsTextView.setText("Decibels: Data not found");
                    luxTextView.setText("Lux: Data not found");
                    recommendationTextView.setText("Recommendation: No data available");
                    timestampTextView.setText("Time: No data available");

                    Toast.makeText(this, "Scan data not found.", Toast.LENGTH_SHORT).show();
                }
                if (scan.getPhotoPath() != null) {
                    File photoFile = new File(scan.getPhotoPath());
                    if (photoFile.exists()) {
                        Bitmap photoBitmap = BitmapFactory.decodeFile(scan.getPhotoPath());
                        roomPhotoImageView.setImageBitmap(photoBitmap);
                        roomPhotoImageView.setVisibility(View.VISIBLE);
                    } else {
                        roomPhotoImageView.setVisibility(View.GONE);
                    }
                } else {
                    roomPhotoImageView.setVisibility(View.GONE);
                }
            });

        }).start();
    }
}