package com.example.onlyfish;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsFragment extends Fragment {

    private enum TimeRange {
        LAST_MONTH,
        LAST_YEAR,
        OVERALL
    }

    private static final String TAG = "StatisticsFragment";

    private RecyclerView recyclerView;
    private StatisticsAdapter adapter;
    private RadioGroup timeRangeRadioGroup;
    private Button addRecordButton;
    private Button homeButton;
    private LambdaHelper lambdaHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        recyclerView = view.findViewById(R.id.statisticsRecyclerView);
        timeRangeRadioGroup = view.findViewById(R.id.timeRangeRadioGroup);
        addRecordButton = view.findViewById(R.id.addRecordButton);
        homeButton = view.findViewById(R.id.home_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StatisticsAdapter(new ArrayList<>()); // Initialize with an empty list
        recyclerView.setAdapter(adapter);

        // Initialize the database helper
        lambdaHelper = LambdaHelper.getInstance(requireContext());

        // Set up time range selection
        timeRangeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            TimeRange timeRange = TimeRange.OVERALL; // Default
            if (checkedId == R.id.lastMonthRadioButton) {
                timeRange = TimeRange.LAST_MONTH;
            } else if (checkedId == R.id.lastYearRadioButton) {
                timeRange = TimeRange.LAST_YEAR;
            }
            loadStatistics(timeRange);
        });


        addRecordButton.setOnClickListener(v -> showAddRecordDialog());


        homeButton.setOnClickListener(v -> navigateToHome());


        // Load initial statistics (Overall)
        loadStatistics(TimeRange.OVERALL);

        return view;
    }

    private void loadStatistics(TimeRange timeRange) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("UserID", lambdaHelper.getUserId());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON request: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        lambdaHelper.sendRequestToLambda(LambdaHelper.LambdaFunction.GET_STATISTICS_DATA, requestBody, new LambdaHelper.LambdaResponseListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "GET_STATISTICS_DATA response: " + response);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Map<String, Object>> records = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Map<String, Object> record = new HashMap<>();
                        record.put("WaterBodyName", jsonObject.getString("WaterBodyName"));
                        record.put("FishCaught", jsonObject.getInt("FishCaught"));
                        record.put("BiggestFish", jsonObject.getDouble("BiggestFish"));
                        String createdAtString = jsonObject.getString("CreatedAt");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        LocalDateTime createdAt = LocalDateTime.parse(createdAtString.substring(0, 19), formatter);
                        record.put("CreatedAt", createdAt);
                        records.add(record);
                    }
                    List<Map<String, Object>> statistics = calculateStatistics(records, timeRange);
                    mainThreadHandler.post(() -> adapter.updateData(statistics));
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON response: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching statistics: " + error);
                Toast.makeText(requireContext(), "Error fetching statistics", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Map<String, Object>> calculateStatistics(List<Map<String, Object>> records, TimeRange timeRange) {
        List<Map<String, Object>> filteredRecords = new ArrayList<>();
        switch (timeRange) {
            case LAST_MONTH:
                YearMonth lastMonth = YearMonth.now().minusMonths(1);
                for (Map<String, Object> record : records) {
                    LocalDateTime recordDate = (LocalDateTime) record.get("CreatedAt");
                    YearMonth recordYearMonth = YearMonth.from(recordDate);
                    if (recordYearMonth.equals(lastMonth)) {
                        filteredRecords.add(record);
                    }
                }
                break;
            case LAST_YEAR:
                LocalDate lastYear = LocalDate.now().minusYears(1);
                for (Map<String, Object> record : records) {
                    LocalDateTime recordDate = (LocalDateTime) record.get("CreatedAt");
                    if (recordDate.toLocalDate().isAfter(lastYear)) {
                        filteredRecords.add(record);
                    }
                }
                break;
            case OVERALL:
                filteredRecords.addAll(records);
                break;
        }

        // Group by water body
        Map<String, List<Map<String, Object>>> waterBodyGroups = new HashMap<>();
        for (Map<String, Object> record : filteredRecords) {
            String waterBody = (String) record.get("WaterBodyName");
            if (!waterBodyGroups.containsKey(waterBody)) {
                waterBodyGroups.put(waterBody, new ArrayList<>());
            }
            waterBodyGroups.get(waterBody).add(record);
        }

        // Calculate statistics for each water body
        List<Map<String, Object>> statistics = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : waterBodyGroups.entrySet()) {
            String waterBody = entry.getKey();
            List<Map<String, Object>> waterBodyRecords = entry.getValue();
            int totalFishCaught = 0;
            double biggestFish = 0.0;
            for (Map<String, Object > record : waterBodyRecords) {
                totalFishCaught += (int) record.get("FishCaught");
                if ((double) record.get("BiggestFish") > biggestFish) {
                    biggestFish = (double) record.get("BiggestFish");
                }
            }
            double averageFishCaught = waterBodyRecords.isEmpty() ? 0.0 : (double) totalFishCaught / waterBodyRecords.size();
            int numberOfRecords = waterBodyRecords.size();

            Map<String, Object> waterBodyStats = new HashMap<>();
            waterBodyStats.put("waterBody", waterBody);
            waterBodyStats.put("averageFishCaught", averageFishCaught);
            waterBodyStats.put("biggestFish", biggestFish);
            waterBodyStats.put("totalFishCaught", totalFishCaught);
            waterBodyStats.put("numberOfRecords", numberOfRecords);
            statistics.add(waterBodyStats);
        }

        return statistics;
    }

    private void showAddRecordDialog() {
        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_pin, null);

        // Find the views in the dialog layout
        EditText waterBodyEditText = dialogView.findViewById(R.id.water_body_edit_text);
        EditText pinNameEditText = dialogView.findViewById(R.id.pin_name_edit_text);
        EditText fishCaughtEditText = dialogView.findViewById(R.id.fish_caught_edit_text);
        EditText biggestFishEditText = dialogView.findViewById(R.id.biggest_fish_edit_text);

        // Hide the pin name field
        pinNameEditText.setVisibility(View.GONE);

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        builder.setTitle("Add New Record");

        // Set the positive button (Add)
        builder.setPositiveButton("Add", (dialog, which) -> {
            // Get the values from the input fields
            String waterBody = waterBodyEditText.getText().toString().trim();
            String fishCaughtStr = fishCaughtEditText.getText().toString().trim();
            String biggestFishStr = biggestFishEditText.getText().toString().trim();

            // Validate required fields
            if (waterBody.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in the required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            int fishCaught = 0;
            if (!fishCaughtStr.isEmpty()) {
                try {
                    fishCaught = Integer.parseInt(fishCaughtStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Invalid number for 'Fish Caught'.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            double biggestFish = 0.0;
            if (!biggestFishStr.isEmpty()) {
                try {
                    biggestFish = Double.parseDouble(biggestFishStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Invalid number for 'Biggest Fish'.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Save the data to the database (including user ID, location, and other details)
            saveRecordData(waterBody, fishCaught, biggestFish);
        });

        // Set the negative button (Cancel)
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.show();
    }

    private void saveRecordData(String waterBody, int fishCaught, double biggestFish) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("UserID", lambdaHelper.getUserId());
            requestBody.put("WaterBodyName", waterBody);
            requestBody.put("FishCaught", fishCaught);
            requestBody.put("BiggestFish", biggestFish);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON request: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        lambdaHelper.sendRequestToLambda(LambdaHelper.LambdaFunction.POST_WATER_BODIES_AND_PINS, requestBody, new LambdaHelper.LambdaResponseListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "POST_WATER_BODIES_AND_PINS response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("message")) {
                        String message = jsonResponse.getString("message");
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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
                Log.e(TAG, "Error saving record: " + error);
                Toast.makeText(requireContext(), "Error saving record", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(); // This will remove the MapFragment from the stack and reveal the HomeFragment
        }
    }
}