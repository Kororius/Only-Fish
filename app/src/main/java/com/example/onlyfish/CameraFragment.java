package com.example.onlyfish;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    private ImageCapture imageCapture;
    private PreviewView viewFinder;
    private ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private LambdaHelper lambdaHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register for camera permission result
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handleCameraPermissionResult
        );

        // Register for gallery activity result
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleGalleryResult
        );

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions();
        }
        lambdaHelper = LambdaHelper.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        viewFinder = view.findViewById(R.id.viewFinder);
        Button captureButton = view.findViewById(R.id.capture_button);
        Button galleryButton = view.findViewById(R.id.gallery_button);
        captureButton.setOnClickListener(v -> takePhoto());
        galleryButton.setOnClickListener(v -> launchGallery());
        return view;
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        cameraPermissionLauncher.launch(REQUIRED_PERMISSIONS);
    }

    private void handleCameraPermissionResult(Map<String, Boolean> result) {
        boolean permissionGranted = true;
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            if (entry.getKey().equals(Manifest.permission.CAMERA) && !entry.getValue()) {
                permissionGranted = false;
                break;
            }
        }
        if (permissionGranted) {
            startCamera();
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            // Handle the case where permission is denied (e.g., navigate back)
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(), cameraSelector, preview, imageCapture
                );

            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
                // Handle camera setup failure (e.g., show error message)
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(requireContext(), "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()));
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        Objects.requireNonNull(requireContext()).getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build();

        imageCapture.takePicture(
                outputOptions,
                Executors.newSingleThreadExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();
                        if (savedUri != null) {
                            Log.d(TAG, "Photo capture succeeded: " + savedUri);
                            handleCapturedImage(savedUri); // Call new method
                        } else {
                            Log.e(TAG, "Photo capture succeeded, but URI is null");
                            // Handle the case where the URI is null (optional)
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(requireContext(), "Photo capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        // Handle the case where photo capture failed (e.g., show error message)
                    }
                }
        );
    }

    private void launchGallery() {
        galleryLauncher.launch("image/*");
    }

    private void handleGalleryResult(Uri uri) {
        if (uri != null) {
            Log.d(TAG, "Image selected from gallery: " + uri);
            handleCapturedImage(uri);
        } else {
            Log.e(TAG, "No image selected from gallery");
        }
    }

    private void handleCapturedImage(Uri imageUri) {
        Log.d(TAG, "Image captured or selected: " + imageUri);
        sendImageToAWS(imageUri);
    }

    private void sendImageToAWS(Uri imageUri) {
        byte[] imageData = convertImageUriToByteArray(imageUri);
        if (imageData == null) {
            Toast.makeText(requireContext(), "Error converting image to byte array", Toast.LENGTH_SHORT).show();
            return;
        }
        lambdaHelper.sendImageToLambda(LambdaHelper.LambdaFunction.GET_FISH_RULES, imageData, new LambdaHelper.LambdaResponseListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "GET_FISH_RULES response: " + response);
                Toast.makeText(requireContext(), "Lambda Response: " + response, Toast.LENGTH_LONG).show();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.length() > 0) {
                        String fishName = jsonObject.getString("FishName");
                        double minLength = jsonObject.getDouble("MinLength");
                        double maxLength = jsonObject.getDouble("MaxLength");
                        String startDate = jsonObject.getString("StartDate");
                        String endDate = jsonObject.getString("EndDate");
                        int isBannedForever = jsonObject.getInt("IsBannedForever");
                        String createdAt = jsonObject.getString("CreatedAt");

                        Intent resultIntent = new Intent(requireContext(), ResultActivity.class);
                        resultIntent.putExtra("imageUri", imageUri.toString());
                        resultIntent.putExtra("fishName", fishName);
                        resultIntent.putExtra("minLength", minLength);
                        resultIntent.putExtra("maxLength", maxLength);
                        resultIntent.putExtra("startDate", startDate);
                        resultIntent.putExtra("endDate", endDate);
                        resultIntent.putExtra("isBannedForever", isBannedForever);
                        resultIntent.putExtra("createdAt", createdAt);
                        startActivity(resultIntent);
                    } else {
                        Log.e(TAG, "No rules found for the fish");
                        Toast.makeText(requireContext(), "No rules found for the fish", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON response: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error sending image to AWS: " + error);
                Toast.makeText(requireContext(), "Error sending image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private byte[] convertImageUriToByteArray(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Error converting image to byte array: InputStream is null");
                return null;
            }

            // Decode the image into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close(); // Close the input stream after decoding

            if (bitmap == null) {
                Log.e(TAG, "Error converting image to byte array: Bitmap decoding failed");
                return null;
            }

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            // Compress the bitmap (adjust quality as needed, 80 is a good starting point)
            // You can experiment with the quality value (0-100)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteBuffer);

            // Recycle the bitmap to free up memory
            bitmap.recycle();

            return byteBuffer.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "Error converting image to byte array: " + e.getMessage(), e);
            return null;
        } catch (SecurityException e) {
            Log.e(TAG, "Security error accessing image URI: " + e.getMessage(), e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error converting image to byte array: " + e.getMessage(), e);
            return null;
        }
    }
}