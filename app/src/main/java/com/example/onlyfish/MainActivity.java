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

public class MainActivity extends AppCompatActivity implements HomeFragment.HomeFragmentListener {

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
        System.out.println("Camera button clicked in MainActivity");
        loadFragment(new CameraFragment());
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
        System.out.println("Map button clicked in MainActivity");
        loadFragment(new MapFragment()); // Load the MapFragment here
    }

    @Override
    public void onStatisticsClick() { // Added
        System.out.println("Statistics button clicked in MainActivity");
        // TODO: Handle statistics button click (e.g., open statistics fragment/activity)
    }

    @Override
    public void onFishingLicenseClick() { // Added
        System.out.println("Fishing License button clicked in MainActivity");
        // TODO: Handle fishing license button click (e.g., open fishing license fragment/activity)
    }

    @Override
    public void onLogoutClick() { // Added
        System.out.println("Logout button clicked in MainActivity");
        // TODO: Handle logout button click (e.g., clear user session, navigate to login screen)
        // For example, to navigate to a login activity:
        // Intent intent = new Intent(this, LoginActivity.class);
        // startActivity(intent);
        // finish(); // Close the main activity
    }
}