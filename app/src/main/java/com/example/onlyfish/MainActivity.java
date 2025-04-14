package com.example.onlyfish;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements HomeFragment.HomeFragmentListener {

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
        // TODO: Handle camera button click (e.g., start camera fragment/activity)
        // For now, let's just print a message to the console
        System.out.println("Camera button clicked in MainActivity");
    }

    @Override
    public void onMapClick() {
        // TODO: Handle map button click (e.g., open map fragment/activity)
        // For now, let's just print a message to the console
        System.out.println("Map button clicked in MainActivity");
    }
}