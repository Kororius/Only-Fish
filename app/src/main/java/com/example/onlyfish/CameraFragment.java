package com.example.onlyfish;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

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
    private ActivityResultLauncher<String[]> activityResultLauncher;

    // Interface for communication with the hosting activity
    public interface CameraFragmentListener {
        void onImageCaptured(Uri imageUri);
    }

    private CameraFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CameraFragmentListener) {
            listener = (CameraFragmentListener) context;
        } else {
            throw new RuntimeException(context + " must implement CameraFragmentListener");
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean permissionGranted = true;
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        if (entry.getKey().equals(Manifest.permission.CAMERA) && !entry.getValue()) {
                            permissionGranted = false;
                            break;
                        }
                    }
                    if (!permissionGranted) {
                        Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_SHORT).show();
                        // Consider informing the activity about the failure
                        if (listener != null) {
                            listener.onImageCaptured(null); // Or a specific error code/URI
                        }
                    } else {
                        startCamera();
                    }
                }
        );

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        viewFinder = view.findViewById(R.id.viewFinder);
        Button captureButton = view.findViewById(R.id.capture_button);
        captureButton.setOnClickListener(v -> takePhoto());
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
        activityResultLauncher.launch(REQUIRED_PERMISSIONS);
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
                // Consider informing the activity about the failure
                if (listener != null) {
                    listener.onImageCaptured(null); // Or a specific error code/URI
                }
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
                            String msg = "Photo capture succeeded: " + savedUri;
                            Log.d(TAG, msg);
                            // Notify the activity about the captured image
                            if (listener != null) {
                                listener.onImageCaptured(savedUri);
                            }
                        } else {
                            Log.e(TAG, "Photo capture succeeded, but URI is null");
                            // Notify the activity about the failure (even though it technically succeeded partially)
                            if (listener != null) {
                                listener.onImageCaptured(null); // Or a specific error code/URI
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(requireContext(), "Photo capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        // Notify the activity about the failure
                        if (listener != null) {
                            listener.onImageCaptured(null); // Or a specific error code/URI
                        }
                    }
                }
        );
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}