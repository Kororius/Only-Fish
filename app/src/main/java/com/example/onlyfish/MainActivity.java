package com.example.onlyfish;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements HomeFragment.HomeFragmentListener, CameraFragment.CameraFragmentListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCameraClick() {
        // For now, let's just print a message to the console
        System.out.println("Camera button clicked in MainActivity");
        loadFragment(new CameraFragment());
    }
    @Override
    public void onImageCaptured(Uri imageUri) {
        if (imageUri != null) {
            Log.d(TAG, "Image captured: " + imageUri);
            // The Toast is removed here
            onImageSelected(imageUri); // Call the new method
        } else {
            Log.e(TAG, "Image capture failed");
            // The Toast is removed here
            // Handle the failure if needed, e.g., navigate back or show an error in the UI
        }
    }

    private void onImageSelected(Uri imageUri) {
        // TODO: Send image to API and get result
        // For now, simulate a result
        String textFromDb = "This is a placeholder text from the database.";

        // Start the new activity to display the image and text
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        intent.putExtra("text", textFromDb);
        startActivity(intent);
    }

    @Override
    public void onMapClick() {
        // TODO: Handle map button click (e.g., open map fragment/activity)
        // For now, let's just print a message to the console
        System.out.println("Map button clicked in MainActivity");
    }
}