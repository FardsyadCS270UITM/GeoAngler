package com.hemantpatel.mpfapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hemantpatel.mpfapp.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Searchlens extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = Searchlens.class.getSimpleName();

    public static Intent newIntent(Context context) {
        Log.d(TAG, "newIntent()");
        return new Intent(context.getApplicationContext(), Searchlens.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    private static final int PERMISSION_STATE = 0;
    private static final int CAMERA_REQUEST = 1;
    private Button imgCamera;
    private ImageView imgResult;
    private Button btnPredict;
    private TextView txtPrediction;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchlens);
        imgCamera = findViewById(R.id.captureButton);
        imgResult = findViewById(R.id.resultImage);
        txtPrediction = findViewById(R.id.textViewPrediction);
        btnPredict = findViewById(R.id.scanButton);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scanButton:
                predict();
                break;
            case R.id.captureButton:
                launchCamera();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        btnPredict.setOnClickListener(this);
        imgCamera.setOnClickListener(this);
        checkPermissions();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        btnPredict.setOnClickListener(null);
        imgCamera.setOnClickListener(null);
    }

    private void launchCamera() {
        Log.d(TAG, "launchCamera()");
        startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST);
    }

    private void predict() {
        Log.d(TAG, "predict()");
        if (bitmap == null) {
            txtPrediction.setText("Error: Capture an image first");
            return;
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        try {
            Log.d(TAG, "try");
            Model model = Model.newInstance(getApplicationContext());
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);
            TensorImage tensorImage = new TensorImage(DataType.UINT8);
            tensorImage.load(bitmap);
            ByteBuffer byteBuffer = tensorImage.getBuffer();

            inputFeature0.loadBuffer(byteBuffer);
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            model.close();

            String prediction = getMax(outputFeature0.getFloatArray());
            txtPrediction.setText(prediction);
            Log.d("Result", Arrays.toString(outputFeature0.getFloatArray()));
        } catch (IOException e) {
            Log.e(TAG, "IOException " + e.getMessage());
            txtPrediction.setText("Error: Unable to load model");
        }
    }

    private String getMax(float[] outputs) {
        Log.d(TAG, "getMax( " + Arrays.toString(outputs) + ")");

        // Check if the output array is not empty and contains at least 4 elements (assuming 4 classes).
        if (outputs.length >= 4) {
            float maxConfidence = outputs[0];
            int maxIndex = 0;

            // Find the index with the maximum confidence.
            for (int i = 1; i < outputs.length; i++) {
                if (outputs[i] > maxConfidence) {
                    maxConfidence = outputs[i];
                    maxIndex = i;
                }
            }

            // Define a threshold for a minimum confidence to consider the prediction valid.
            float thresholdConfidence = 0.5f;

            // If the maximum confidence is below the threshold, consider it as an unknown prediction.
            if (maxConfidence < thresholdConfidence) {
                return "Unknown";
            } else {
                // Return the label based on the index with the maximum confidence.
                switch (maxIndex) {
                    case 0:
                        return "BARBODES";
                    case 1:
                        return "CAT FISH";
                    case 2:
                        return "Mackerel";
                    case 3:
                        return "NAPOLEON WRASSE";
                    default:
                        return "Unknown";
                }
            }
        } else {
            // The output array is not of the expected size.
            return "Error: Invalid output format";
        }
    }

    private void checkPermissions() {
        String[] manifestPermissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            manifestPermissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        } else {
            manifestPermissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
        for (String permission : manifestPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission Granted " + permission);
            }
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "Permission Denied " + permission);
                requestPermissions();
            }
        }
    }

    private void requestPermissions() {
        Log.d(TAG, "requestPermissions()");
        String[] manifestPermissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            manifestPermissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        } else {
            manifestPermissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        ActivityCompat.requestPermissions(
                this,
                manifestPermissions,
                PERMISSION_STATE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "PermissionsResult requestCode " + requestCode);
        Log.d(TAG, "PermissionsResult permissions " + Arrays.toString(permissions));
        Log.d(TAG, "PermissionsResult grantResults " + Arrays.toString(grantResults));
        if (requestCode == PERMISSION_STATE) {
            for (int grantResult : grantResults) {
                switch (grantResult) {
                    case PackageManager.PERMISSION_GRANTED:
                        Log.d(TAG, "PermissionsResult grantResult Allowed " + grantResult);
                        break;
                    case PackageManager.PERMISSION_DENIED:
                        Log.d(TAG, "PermissionsResult grantResult Denied " + grantResult);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode " + requestCode + " resultCode" + resultCode + "data " + data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imgResult.setImageBitmap(bitmap);
            txtPrediction.setText(""); // Clear any previous prediction when a new image is captured.
        }
    }
}
