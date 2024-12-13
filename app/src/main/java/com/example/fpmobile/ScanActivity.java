package com.example.fpmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fpmobile.database.ScanDatabase;
import com.example.fpmobile.database.ScanEntity;

import java.io.IOException;
import java.util.Locale;

public class ScanActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "ScanActivity";
    private static final int SCAN_DURATION = 3000; // 3 seconds for scanning
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final long ANIMATION_DURATION = 400;

    // State variables
    private float currentLux = -1f;
    private boolean isScanning = false;
    private boolean permissionToRecordAccepted = false;
    private long lastLightLogTime = 0;

    // UI Components
    private TextView tvDecibels, tvLightLevel, tvRecommendation;
    private View resultCard;
    private TextView btnStartScan;
    private Button btnToHome;

    // Sensors and Helpers
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private CameraHelper cameraHelper;
    private String currentPhotoPath;


    private AudioRecorderHelper audioRecorderHelper;
    private TFLiteModelHelper tfliteHelper;

    // Animation related fields
    private ObjectAnimator pulseAnimator;
    private ObjectAnimator rotationAnimator;

    // Temporary variables to store scan results
    private float tempDecibels;
    private float tempLux;
    private String tempRecommendation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initializeComponents();
        setupAnimations();
        cameraHelper = new CameraHelper();
    }

    private void initializeComponents() {
        // Initialize UI components
        tvDecibels = findViewById(R.id.tvDecibels);
        tvLightLevel = findViewById(R.id.tvLightLevel);
        tvRecommendation = findViewById(R.id.tvRecommendation);
        resultCard = findViewById(R.id.resultCard);
        btnStartScan = findViewById(R.id.btnStartScan);
        btnToHome = findViewById(R.id.btnToHome);

        // Initialize sensors
        initializeSensors();

        // Initialize TFLite helper
        initializeTFLiteHelper();

        // Set up button listeners
        setupButtonListeners();

        // Request necessary permissions
        requestPermissions();
    }


    private void registerSensors() {
        // Register light sensor
        if (lightSensor != null) {
            boolean isLightSensorRegistered = sensorManager.registerListener(
                    this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            if (!isLightSensorRegistered) {
                Log.e(TAG, "Failed to register light sensor listener");
                tvRecommendation.setText("Light sensor is not responding");
            }
        }
    }

    private void setupAnimations() {
        // Setup pulse animation for scan button
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f);

        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(btnStartScan, scaleX, scaleY);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.start();

        // Setup rotation animation for scanning state
        rotationAnimator = ObjectAnimator.ofFloat(btnStartScan, View.ROTATION, 0f, 360f);
        rotationAnimator.setDuration(SCAN_DURATION);
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotationAnimator.setInterpolator(new LinearInterpolator());
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Log.e(TAG, "Device does not have a light sensor");
            tvRecommendation.setText("Light sensor not available");
        }

        audioRecorderHelper = new AudioRecorderHelper();
        if (!audioRecorderHelper.isInitialized()) {
            Log.e(TAG, "Audio Recorder failed to initialize");
            tvRecommendation.setText("Audio recording is not supported on this device");
            btnStartScan.setEnabled(false);
        }
    }

    private void initializeTFLiteHelper() {
        try {
            tfliteHelper = new TFLiteModelHelper(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize TFLite model", e);
            Toast.makeText(this, "Failed to initialize analysis model", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonListeners() {
        btnStartScan.setOnClickListener(v -> {
            if (permissionToRecordAccepted) {
                startScan();
            } else {
                requestPermissions();
            }
        });

        btnToHome.setOnClickListener(v -> navigateToHome());
    }

    private void startScan() {

        if (isScanning) {
            Log.w(TAG, "Scan already in progress");
            return;
        }

        Log.i(TAG, "Starting scan...");
        isScanning = true;

        pulseAnimator.cancel();
        rotationAnimator.start();
        animateCardOut();

        audioRecorderHelper.startRecording();
        registerSensors();

        new Handler().postDelayed(this::processScanResults, SCAN_DURATION);
    }

    private void saveScanToDatabase(float decibels, float lux, String recommendation, String photoPath) {
        ScanEntity newScan = new ScanEntity(
                decibels,
                lux,
                recommendation,
                System.currentTimeMillis(),
                photoPath
        );

        new Thread(() -> {
            try {
                ScanDatabase.getInstance(this).scanDao().insertScan(newScan);
                runOnUiThread(() -> {
                    updateResults(decibels, lux, recommendation);
                    sendResultsToMain(decibels, lux, recommendation);
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to save scan results", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void processScanResults() {
        float decibels = audioRecorderHelper.getAmplitude();
        audioRecorderHelper.stopRecording();
        sensorManager.unregisterListener(this);

        // Validate measurements
        decibels = validateDecibels(decibels);
        currentLux = validateLux(currentLux);

        // Generate recommendation
        String recommendation = generateRecommendationWithModel(decibels, currentLux);

        // Update UI with results first
        updateResults(decibels, currentLux, recommendation);

        // Show dialog to take photo - this will handle saving to database
        showTakePhotoDialog(decibels, currentLux, recommendation);

        // Reset scanning state
        isScanning = false;
        stopScanningAnimation();
    }

    private float validateDecibels(float decibels) {
        if (decibels <= 0) {
            Log.w(TAG, "Invalid decibel reading, using default value");
            return 0;
        }
        return decibels;
    }

    private void showTakePhotoDialog(float decibels, float lux, String recommendation) {
        // Store measurements first since we might need them after permission check
        tempDecibels = decibels;
        tempLux = lux;
        tempRecommendation = recommendation;

        // Check camera permission before showing dialog
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            checkCameraPermission();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Take Room Photo")
                .setMessage("Would you like to take a photo of the room?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Store measurements temporarily
                    tempDecibels = decibels;
                    tempLux = lux;
                    tempRecommendation = recommendation;

                    try {
                        Intent cameraIntent = cameraHelper.createCameraIntent(this);
                        if (cameraIntent != null) {
                            startActivityForResult(cameraIntent, CameraHelper.REQUEST_IMAGE_CAPTURE);
                        }
                    } catch (IOException e) {
                        Toast.makeText(this, "Could not start camera", Toast.LENGTH_SHORT).show();
                        saveResultsWithoutPhoto(decibels, lux, recommendation);
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    saveResultsWithoutPhoto(decibels, lux, recommendation);
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Photo was taken successfully
            currentPhotoPath = cameraHelper.getCurrentPhotoPath();
            saveScanToDatabase(tempDecibels, tempLux, tempRecommendation, currentPhotoPath);
        } else if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE) {
            // Camera was cancelled
            saveResultsWithoutPhoto(tempDecibels, tempLux, tempRecommendation);
        }
    }

    private void saveResultsWithoutPhoto(float decibels, float lux, String recommendation) {
        saveScanToDatabase(decibels, lux, recommendation, null);
    }

    private float validateLux(float lux) {
        if (lux < 0) {
            Log.w(TAG, "Invalid lux reading, using default value");
            return 0;
        }
        return lux;
    }

    private String generateRecommendationWithModel(float decibels, float lux) {
        try {
            int comfortLevel = tfliteHelper.predictComfortLevel(decibels, currentLux);
            return generateRecommendation(decibels, lux, comfortLevel);
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate ML-based recommendation", e);
            return generateBasicRecommendation(decibels, lux);
        }
    }

    private void updateResults(float decibels, float lux, String recommendation) {
        tvDecibels.setText(String.format(Locale.getDefault(), "Decibels: %.2f dB", decibels));
        tvLightLevel.setText(String.format(Locale.getDefault(), "Lux: %.2f", lux));
        tvRecommendation.setText(recommendation);

        animateCardIn();

        // Send results back to MainActivity
        sendResultsToMain(decibels, lux, recommendation);
    }

    private void sendResultsToMain(float decibels, float lux, String recommendation) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SCAN_DECIBELS", decibels);
        resultIntent.putExtra("SCAN_LUX", lux);
        resultIntent.putExtra("COMFORT_RECOMMENDATION", recommendation);
        setResult(RESULT_OK, resultIntent);
    }

    private void animateCardOut() {
        if (resultCard.getVisibility() == View.VISIBLE) {
            AnimatorSet animatorSet = new AnimatorSet();

            ObjectAnimator slideDown = ObjectAnimator.ofFloat(
                    resultCard, View.TRANSLATION_Y, 0f, 500f);
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(
                    resultCard, View.ALPHA, 1f, 0f);

            animatorSet.playTogether(slideDown, fadeOut);
            animatorSet.setDuration(ANIMATION_DURATION);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resultCard.setVisibility(View.GONE);
                }
            });
            animatorSet.start();
        }
    }

    private void animateCardIn() {
        resultCard.setVisibility(View.VISIBLE);
        resultCard.setAlpha(0f);
        resultCard.setTranslationY(500f);

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator slideUp = ObjectAnimator.ofFloat(
                resultCard, View.TRANSLATION_Y, 500f, 0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(
                resultCard, View.ALPHA, 0f, 1f);

        animatorSet.playTogether(slideUp, fadeIn);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.setInterpolator(new OvershootInterpolator(0.8f));
        animatorSet.start();
    }

    private void stopScanningAnimation() {
        rotationAnimator.cancel();
        btnStartScan.animate()
                .rotation(0f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> pulseAnimator.start())
                .start();
    }

    // Your existing recommendation methods remain the same
    private String generateRecommendation(float decibels, float lux, int comfortLevel) {
        switch (comfortLevel) {
            case 1: // Sangat Baik
                return "The environment is excellent for your baby's comfort";
            case 2: // Baik
                return lux < 300
                        ? "Good environment, but consider adding more light"
                        : "Good environment, but try to reduce noise slightly";
            case 3: // Buruk
                return "Environment needs improvement. " +
                        (decibels > 70 ? "Reduce noise levels. " : "") +
                        (lux < 300 ? "Increase lighting. " : "");
            case 4: // Sangat Buruk
                return "Environment requires immediate attention. Improve both lighting and noise conditions";
            default:
                return generateBasicRecommendation(decibels, lux);
        }
    }

    private String generateBasicRecommendation(float decibels, float lux) {
        if (decibels <= 70 && lux >= 300) {
            return "The environment is good for your baby";
        } else if (decibels <= 70) {
            return "The environment is quiet, but needs more light";
        } else if (lux >= 300) {
            return "The lighting is good, but the environment is too noisy";
        } else {
            return "Both noise and lighting conditions need improvement";
        }
    }

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        boolean needsPermissions = false;
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                needsPermissions = true;
                break;
            }
        }

        if (needsPermissions) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            return;
        }

        permissionToRecordAccepted = true;
    }

    private static final int PERMISSION_REQUEST_CAMERA = 2;

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Handle RECORD_AUDIO permission result
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionToRecordAccepted = true;
            } else {
                tvRecommendation.setText("Audio recording permission is required");
                btnStartScan.setEnabled(false);
            }
            }
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with camera operation
                showTakePhotoDialog(tempDecibels, tempLux, tempRecommendation);
            } else {
                // Permission denied, proceed without photo
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                saveResultsWithoutPhoto(tempDecibels, tempLux, tempRecommendation);
            }
        }

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LIGHT:
                currentLux = event.values[0];
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastLightLogTime > 1000) {
                    Log.d(TAG, "Light sensor value: " + currentLux);
                    lastLightLogTime = currentTime;
                }
                break;
                
            default:
                Log.w(TAG, "Unexpected sensor type: " + event.sensor.getType());
                break;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action needed for accuracy changes
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecorderHelper != null) {
            audioRecorderHelper.release();
        }
        sensorManager.unregisterListener(this);
        pulseAnimator.cancel();
        if (rotationAnimator.isRunning()) {
            rotationAnimator.cancel();
        }
    }

    private void navigateToHome() {
        finish();
    }
}