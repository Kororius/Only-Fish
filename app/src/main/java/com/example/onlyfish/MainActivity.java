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
        Log.d(TAG, "Camera button clicked in MainActivity");
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
        Log.d(TAG, "Map button clicked in MainActivity");
        loadFragment(new MapFragment());
    }

    @Override
    public void onStatisticsClick() {
        Log.d(TAG, "Statistics button clicked in MainActivity");
        loadFragment(new StatisticsFragment());
    }

    @Override
    public void onFishingLicenseClick() {
        Log.d(TAG, "Fishing License button clicked in MainActivity");
        loadFragment(new FishingLicenseFragment()); // Load the FishingLicenseFragment
    }

    @Override
    public void onLogoutClick() {
        Log.d(TAG, "Logout button clicked in MainActivity");
        // TODO: Handle logout button click (e.g., clear user session, navigate to login screen)
        // For example, to navigate to a login activity:
        // Intent intent = new Intent(this, LoginActivity.class);
        // startActivity(intent);
        // finish(); // Close the main activity
    }
}