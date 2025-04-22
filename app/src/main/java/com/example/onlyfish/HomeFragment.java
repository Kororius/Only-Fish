package com.example.onlyfish;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    // Define an interface for activity interaction
    public interface HomeFragmentListener {
        void onCameraClick();
        void onMapClick();
        void onStatisticsClick(); // Added
        void onFishingLicenseClick(); // Added
        void onLogoutClick(); // Added
    }

    private HomeFragmentListener listener;

    // Attach the listener when the fragment is attached to an activity
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeFragmentListener) {
            listener = (HomeFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement HomeFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button cameraButton = view.findViewById(R.id.camera_button);
        Button mapButton = view.findViewById(R.id.map_button);
        Button statisticsButton = view.findViewById(R.id.statistics_button); // Added
        Button fishingLicenseButton = view.findViewById(R.id.fishing_license_button); // Added
        Button logoutButton = view.findViewById(R.id.logout_button); // Added

        cameraButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCameraClick();
            }
        });

        mapButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMapClick();
            }
        });

        statisticsButton.setOnClickListener(v -> { // Added
            if (listener != null) {
                listener.onStatisticsClick();
            }
        });

        fishingLicenseButton.setOnClickListener(v -> { // Added
            if (listener != null) {
                listener.onFishingLicenseClick();
            }
        });

        logoutButton.setOnClickListener(v -> { // Added
            if (listener != null) {
                listener.onLogoutClick();
            }
        });

        return view;
    }

    // Detach the listener when the fragment is detached
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}