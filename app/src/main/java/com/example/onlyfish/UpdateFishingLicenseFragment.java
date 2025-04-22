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

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateFishingLicenseFragment extends Fragment {

    private static final int PICK_PDF_FILE = 2; // Request code for file picker

    private Button uploadPdfButton;
    private Button manualEntryButton;
    private EditText licenseNumberEditText;
    private EditText nameEditText;
    private EditText surnameEditText;
    private EditText personalIdEditText;
    private EditText expiryDateEditText;
    private Button saveButton;
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

        uploadPdfButton = view.findViewById(R.id.upload_pdf_button);
        manualEntryButton = view.findViewById(R.id.manual_entry_button);
        licenseNumberEditText = view.findViewById(R.id.license_number_edit_text);
        nameEditText = view.findViewById(R.id.name_edit_text);
        surnameEditText = view.findViewById(R.id.surname_edit_text);
        personalIdEditText = view.findViewById(R.id.personal_id_edit_text);
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

        saveButton.setOnClickListener(v -> {
            // Get data from EditText fields
            String licenseNumber = licenseNumberEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String surname = surnameEditText.getText().toString().trim();
            String personalId = personalIdEditText.getText().toString().trim();
            String expiryDate = expiryDateEditText.getText().toString().trim();

            // TODO: Validate input (check for empty fields, correct formats, etc.)

            // TODO: Save the data to the database
            saveLicenseData(licenseNumber, name, surname, personalId, expiryDate);

            // Navigate back to the FishingLicenseFragment or HomeFragment
            navigateToFishingLicenseFragment(); // Or navigateToHome()
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
                extractDataFromPdf(text.toString());
            } else {
                Toast.makeText(requireContext(), "Could not open PDF file", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("PDF_ERROR", "Error reading PDF: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error reading PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void extractDataFromPdf(String pdfText) {
        Log.d("PDF_TEXT", "Extracted PDF Text:\n" + pdfText);

        String licenseNumber = extractLicenseNumber(pdfText);
        String nameSurname = extractNameSurname(pdfText);
        String personalId = extractPersonalId(pdfText);
        String expiryDate = extractExpiryDate(pdfText);

        // Display extracted data in a Toast
        String message = "License Number: " + licenseNumber + "\n" +
                "Name, Surname: " + nameSurname + "\n" +
                "Personal ID: " + personalId + "\n" +
                "Expiry Date: " + expiryDate;

        // TODO: Store extracted data in the database
        Log.d("PDF_EXTRACTION", message);
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

    private String extractPersonalId(String text) {
        Pattern pattern = Pattern.compile("\\d{8}\\*{3}");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
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
        personalIdEditText.setVisibility(visibility);
        expiryDateEditText.setVisibility(visibility);
        saveButton.setVisibility(visibility);
        ((View) licenseNumberEditText.getParent()).requestLayout();
    }

    private void saveLicenseData(String licenseNumber, String name, String surname, String personalId, String expiryDate) {
        // TODO: Implement database saving logic here
        //  - Get user ID (if applicable)
        //  - Create a database entry or update existing entry
        //  - Handle potential errors
        Toast.makeText(requireContext(), "Saving data (not implemented yet)", Toast.LENGTH_SHORT).show();
    }

    private void navigateToFishingLicenseFragment() {
        // Navigate back to the FishingLicenseFragment
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    }
}