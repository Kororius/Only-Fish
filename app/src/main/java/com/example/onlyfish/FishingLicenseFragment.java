package com.example.onlyfish;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

public class FishingLicenseFragment extends Fragment {

    private static final String TAG = "FishingLicenseFragment";
    private TextView licenseNumberTextView;
    private TextView nameTextView;
    private TextView surnameTextView;
    private TextView personalIdTextView;
    private TextView expiryDateTextView;
    private Button updateLicenseButton;
    private Button homeButton;
    private Button addLicenseButton;
    private LambdaHelper lambdaHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fishing_license, container, false);

        lambdaHelper = LambdaHelper.getInstance(requireContext());

        licenseNumberTextView = view.findViewById(R.id.license_number_text_view);
        nameTextView = view.findViewById(R.id.name_text_view);
        surnameTextView = view.findViewById(R.id.surname_text_view);
        personalIdTextView = view.findViewById(R.id.personal_id_text_view);
        expiryDateTextView = view.findViewById(R.id.expiry_date_text_view);
        updateLicenseButton = view.findViewById(R.id.update_license_button);
        homeButton = view.findViewById(R.id.home_button);
        addLicenseButton = view.findViewById(R.id.add_license_button);

        homeButton.setOnClickListener(v -> navigateToHome());
        addLicenseButton.setOnClickListener(v -> navigateToUpdateLicenseFragment());
        updateLicenseButton.setOnClickListener(v -> navigateToUpdateLicenseFragment());

        if (lambdaHelper.getHasLicense() == 1) {
            fetchLicenseInfo();
        } else {
            showNoLicenseLayout();
        }

        return view;
    }

    private void fetchLicenseInfo() {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("UserID", lambdaHelper.getUserId());
        } catch (JSONException e) {
            Log.e(TAG, "Error adding UserID to request: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Request body: " + requestBody.toString());

        lambdaHelper.sendRequestToLambda(LambdaHelper.LambdaFunction.GET_LICENCE, requestBody, new LambdaHelper.LambdaResponseListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "License info response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    // Check if the response has the expected keys
                    if (jsonResponse.has("LicenseNumber") && jsonResponse.has("Name") && jsonResponse.has("Surname") && jsonResponse.has("PersonalID") && jsonResponse.has("ExpiryDate")) {
                        String licenseNumber = jsonResponse.getString("LicenseNumber");
                        String name = jsonResponse.getString("Name");
                        String surname = jsonResponse.getString("Surname");
                        String personalId = jsonResponse.getString("PersonalID");
                        String expiryDate = jsonResponse.getString("ExpiryDate");
                        displayLicenseInfo(licenseNumber, name, surname, personalId, expiryDate);
                    } else {
                        Log.e(TAG, "Invalid response format: missing required keys");
                        Toast.makeText(getContext(), "Error: Invalid response format", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON response: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error: Could not parse license data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching license info: " + error);
                Toast.makeText(getContext(), "Error: Could not fetch license data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayLicenseInfo(String licenseNumber, String name, String surname, String personalId, String expiryDate) {
        licenseNumberTextView.setText("License Number: " + licenseNumber);
        nameTextView.setText("Name: " + name);
        surnameTextView.setText("Surname: " + surname);
        personalIdTextView.setText("Personal ID: " + personalId);
        expiryDateTextView.setText("Expiry Date: " + expiryDate);
        licenseNumberTextView.setVisibility(View.VISIBLE);
        nameTextView.setVisibility(View.VISIBLE);
        surnameTextView.setVisibility(View.VISIBLE);
        personalIdTextView.setVisibility(View.VISIBLE);
        expiryDateTextView.setVisibility(View.VISIBLE);
        updateLicenseButton.setVisibility(View.VISIBLE);
        addLicenseButton.setVisibility(View.GONE);
    }

    private void showNoLicenseLayout() {
        licenseNumberTextView.setVisibility(View.GONE);
        nameTextView.setVisibility(View.GONE);
        surnameTextView.setVisibility(View.GONE);
        personalIdTextView.setVisibility(View.GONE);
        expiryDateTextView.setVisibility(View.GONE);
        updateLicenseButton.setVisibility(View.GONE);
        addLicenseButton.setVisibility(View.VISIBLE);
    }

    private void navigateToUpdateLicenseFragment() {
        UpdateFishingLicenseFragment fragment = new UpdateFishingLicenseFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToHome() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    }
}