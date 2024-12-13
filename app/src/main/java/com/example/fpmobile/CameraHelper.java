package com.example.fpmobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraHelper {
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentPhotoPath;

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    public Intent createCameraIntent(Activity activity) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile = createImageFile(activity);
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activity,
                        "com.example.fpmobile.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                return takePictureIntent;
            }
        }
        return null;
    }

    private File createImageFile(Activity activity) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "SCAN_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}