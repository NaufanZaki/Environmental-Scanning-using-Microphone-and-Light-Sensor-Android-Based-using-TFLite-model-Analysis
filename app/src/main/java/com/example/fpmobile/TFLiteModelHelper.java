package com.example.fpmobile;

import android.content.Context;
import org.tensorflow.lite.Interpreter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;

public class TFLiteModelHelper {
    private static final String MODEL_PATH = "comfort_model.tflite";
    private final Interpreter interpreter;

    public TFLiteModelHelper(Context context) throws Exception {
        interpreter = new Interpreter(loadModelFile(context, MODEL_PATH));
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(context.getAssets().openFd(modelPath).getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = context.getAssets().openFd(modelPath).getStartOffset();
        long declaredLength = context.getAssets().openFd(modelPath).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public int predictComfortLevel(float noise, float brightness) {
        // Normalize inputs (assuming MinMaxScaler was used during training)
        float normalizedNoise = noise / 120;  // Assuming max noise level is 120 dB
        float normalizedBrightness = brightness / 1000;  // Assuming max brightness is 1000 lux

        // Prepare input and output arrays
        float[][] input = new float[1][2];
        input[0][0] = normalizedNoise;
        input[0][1] = normalizedBrightness;

        float[][] output = new float[1][4]; // 4 classes: Sangat Baik, Baik, Buruk, Sangat Buruk

        // Run inference
        interpreter.run(input, output);

        // Find the class with the highest probability
        int predictedClass = 0;
        float maxProbability = output[0][0];
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > maxProbability) {
                maxProbability = output[0][i];
                predictedClass = i;
            }
        }

        return predictedClass + 1; // Return class (1: Sangat Baik, 2: Baik, 3: Buruk, 4: Sangat Buruk)
    }
}