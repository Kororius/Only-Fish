package com.example.onlyfish;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements HomeFragment.HomeFragmentListener {

    private static final String TAG = "MainActivity";
    private LambdaHelper lambdaHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lambdaHelper = LambdaHelper.getInstance(this);

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

    @Override
    public void onMapClick() {
        Log.d(TAG, "Map button clicked in MainActivity"); //need UserID
        loadFragment(new MapFragment());
    }

    @Override
    public void onStatisticsClick() {
        Log.d(TAG, "Statistics button clicked in MainActivity"); //need UserID
        loadFragment(new StatisticsFragment());
    }

    @Override
    public void onFishingLicenseClick() {
        Log.d(TAG, "Fishing License button clicked in MainActivity"); //need UserID
        loadFragment(new FishingLicenseFragment());
    }

    @Override
    public void onLogoutClick() {
        Log.d(TAG, "Logout button clicked in MainActivity");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        lambdaHelper.shutdown();
    }
}