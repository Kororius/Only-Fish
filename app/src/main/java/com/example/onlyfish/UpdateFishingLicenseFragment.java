package com.example.onlyfish;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateFishingLicenseFragment extends Fragment {

    private static final String TAG = "UpdateFishingLicenseFragment";
    private static final int PICK_PDF_FILE = 2; // Request code for file picker

    private Button uploadPdfButton;
    private Button manualEntryButton;
    private Button navigateBackButton;
    private EditText licenseNumberEditText;
    private EditText nameEditText;
    private EditText surnameEditText;
    private EditText expiryDateEditText;
    private Button saveButton;
    private LambdaHelper lambdaHelper;
    private final ActivityResultLauncher<Intent> pickPdfLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                Uri pdfUri = data.getData();
                                readPdfText(pdfUri);
                            }
                        } else {
                            Toast.makeText(requireContext(), "PDF selection cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_fishing_license, container, false);

        lambdaHelper = LambdaHelper.getInstance(requireContext());

        uploadPdfButton = view.findViewById(R.id.upload_pdf_button);
        manualEntryButton = view.findViewById(R.id.manual_entry_button);
        navigateBackButton = view.findViewById(R.id.back_button);
        licenseNumberEditText = view.findViewById(R.id.license_number_edit_text);
        nameEditText = view.findViewById(R.id.name_edit_text);
        surnameEditText = view.findViewById(R.id.surname_edit_text);
        expiryDateEditText = view.findViewById(R.id.expiry_date_edit_text);
        saveButton = view.findViewById(R.id.save_button);

        // Initially hide manual entry fields and save button
        setManualEntryVisibility(false);

        uploadPdfButton.setOnClickListener(v -> {
            openFilePicker();
        });

        manualEntryButton.setOnClickListener(v -> {
            setManualEntryVisibility(true);
        });

        navigateBackButton.setOnClickListener(v -> {
            navigateBack();
        });

        saveButton.setOnClickListener(v -> {
            // Get data from EditText fields
            String licenseNumber = licenseNumberEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String surname = surnameEditText.getText().toString().trim();
            String expiryDate = expiryDateEditText.getText().toString().trim();

            // TODO: Validate input (check for empty fields, correct formats, etc.)

            // Save the data to the database
            saveLicenseData(licenseNumber, name, surname, expiryDate);
        });

        return view;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        pickPdfLauncher.launch(intent);
    }

    private void readPdfText(Uri pdfUri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(pdfUri)) {
            if (inputStream != null) {
                PdfReader reader = new PdfReader(inputStream);
                PdfDocument pdfDocument = new PdfDocument(reader);
                StringBuilder text = new StringBuilder();
                for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
                    text.append(PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i)));
                }
                pdfDocument.close();
                extractDataAndSave(text.toString());
            } else {
                Toast.makeText(requireContext(), "Could not open PDF file", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("PDF_ERROR", "Error reading PDF: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error reading PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void extractDataAndSave(String pdfText) {
        Log.d("PDF_TEXT", "Extracted PDF Text:\n" + pdfText);

        String licenseNumber = extractLicenseNumber(pdfText);
        String nameSurname = extractNameSurname(pdfText);
        String expiryDate = extractExpiryDate(pdfText);
        String[] parts = nameSurname.split(" ");
        String name = "";
        String surname = "";
        if (parts.length >= 2) {
            name = parts[0];
            surname = parts[1];
        }
        // Save the data immediately
        saveLicenseData(licenseNumber, name, surname, expiryDate);
    }

    private String extractLicenseNumber(String text) {
        Pattern pattern = Pattern.compile("Nr\\. (\\d+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "N/A";
    }

    private String extractNameSurname(String text) {
        Pattern pattern = Pattern.compile("Žvejo mėgėjo bilieto savininkas: (.*?)(?:, A/k)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "N/A";
    }


    private String extractExpiryDate(String text) {
        Pattern pattern = Pattern.compile("iki (\\d{4}\\.\\d{2}\\.\\d{2})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "N/A";
    }

    private void setManualEntryVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        licenseNumberEditText.setVisibility(visibility);
        nameEditText.setVisibility(visibility);
        surnameEditText.setVisibility(visibility);
        expiryDateEditText.setVisibility(visibility);
        saveButton.setVisibility(visibility);
        if (licenseNumberEditText.getParent() != null) {
            ((View) licenseNumberEditText.getParent()).requestLayout();
        }    }

    private void saveLicenseData(String licenseNumber, String name, String surname, String expiryDate) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("UserID", lambdaHelper.getUserId());
            requestBody.put("LicenseNumber", licenseNumber);
            requestBody.put("Name", name);
            requestBody.put("Surname", surname);
            requestBody.put("ExpiryDate", expiryDate);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON request: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        lambdaHelper.sendRequestToLambda(LambdaHelper.LambdaFunction.POST_LICENCE, requestBody, new LambdaHelper.LambdaResponseListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "License save response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    // Directly access the "message" key
                    if (jsonResponse.has("message")) {
                        String message = jsonResponse.getString("message");
                        if (message.contains("HasLicense flag updated")) {
                            lambdaHelper.setHasLicense(1);
                        }
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        navigateToFishingLicenseFragment();
                    } else {
                        Log.e(TAG, "Invalid response format: missing 'message'");
                        Toast.makeText(getContext(), "Error: Invalid response format", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON response: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error saving license: " + error);
                Toast.makeText(requireContext(), "Error saving license", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToFishingLicenseFragment() {
        // Navigate back to the FishingLicenseFragment
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    }

    private void navigateBack() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(); 
        }
    }
}