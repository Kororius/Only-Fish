package com.example.onlyfish;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FishingLicenseFragment extends Fragment {

    private TextView licenseNumberTextView;
    private TextView nameTextView;
    private TextView surnameTextView;
    private TextView personalIdTextView;
    private TextView expiryDateTextView;
    private Button updateLicenseButton;
    private Button homeButton;

    // TODO: Replace with actual data retrieval from database
    private boolean hasLicense = false; // Simulate user having a license
    private String licenseNumber = "LTF123456789";
    private String name = "Jonas";
    private String surname = "Jonaitis";
    private String personalId = "12345678***"; // Last 3 digits hidden
    private String expiryDate = "2025-12-31";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fishing_license, container, false);

        licenseNumberTextView = view.findViewById(R.id.license_number_text_view);
        nameTextView = view.findViewById(R.id.name_text_view);
        surnameTextView = view.findViewById(R.id.surname_text_view);
        personalIdTextView = view.findViewById(R.id.personal_id_text_view);
        expiryDateTextView = view.findViewById(R.id.expiry_date_text_view);
        updateLicenseButton = view.findViewById(R.id.update_license_button);
        homeButton = view.findViewById(R.id.home_button);

        // Check if user has a license
        if (hasLicense) {
            displayLicenseInfo();
        } else {
            // Navigate to update screen if no license
            navigateToUpdateLicenseFragment();
        }

        updateLicenseButton.setOnClickListener(v -> navigateToUpdateLicenseFragment());

        homeButton.setOnClickListener(v -> navigateToHome());

        return view;
    }

    private void displayLicenseInfo() {
        licenseNumberTextView.setText("License Number: " + licenseNumber);
        nameTextView.setText("Name: " + name);
        surnameTextView.setText("Surname: " + surname);
        personalIdTextView.setText("Personal ID: " + personalId);
        expiryDateTextView.setText("Expiry Date: " + expiryDate);
    }

    private void navigateToUpdateLicenseFragment() {
        // TODO: Replace with actual navigation to update fragment
        // For now, just show a toast
        //  Replace this with actual fragment transaction
        UpdateFishingLicenseFragment fragment = new UpdateFishingLicenseFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment); // Assuming you have a fragment container in your activity
        transaction.addToBackStack(null); // Add to back stack so you can navigate back
        transaction.commit();
    }

    private void navigateToHome() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(); // This will remove the FishingLicenseFragment from the stack and reveal the HomeFragment
        }
    }
}