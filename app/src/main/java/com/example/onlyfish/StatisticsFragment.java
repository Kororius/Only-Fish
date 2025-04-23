package com.example.onlyfish;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.YearMonth;
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

    private RecyclerView recyclerView;
    private StatisticsAdapter adapter;
    private RadioGroup timeRangeRadioGroup;
    private Button addRecordButton;
    private DatabaseHelper databaseHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        recyclerView = view.findViewById(R.id.statisticsRecyclerView);
        timeRangeRadioGroup = view.findViewById(R.id.timeRangeRadioGroup);
        addRecordButton = view.findViewById(R.id.addRecordButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StatisticsAdapter(new ArrayList<>()); // Initialize with an empty list
        recyclerView.setAdapter(adapter);

        // Initialize the database helper
        databaseHelper = new DatabaseHelper();

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

        // Set up add record button
        addRecordButton.setOnClickListener(v -> {
            // Navigate to MapFragment
            MapFragment fragment = new MapFragment();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Load initial statistics (Overall)
        loadStatistics(TimeRange.OVERALL);

        return view;
    }

    private void loadStatistics(TimeRange timeRange) {
        executorService.execute(() -> {
            // Get data from remote database
            List<Map<String, Object>> records = databaseHelper.getAllRecords();
            List<Map<String, Object>> statistics = calculateStatistics(records, timeRange);
            mainThreadHandler.post(() -> adapter.updateData(statistics));
        });
    }

    private List<Map<String, Object>> calculateStatistics(List<Map<String, Object>> records, TimeRange timeRange) {
        List<Map<String, Object>> filteredRecords = new ArrayList<>();
        switch (timeRange) {
            case LAST_MONTH:
                YearMonth lastMonth = YearMonth.now().minusMonths(1);
                for (Map<String, Object> record : records) {
                    LocalDate recordDate = (LocalDate) record.get("date");
                    YearMonth recordYearMonth = YearMonth.from(recordDate);
                    if (recordYearMonth.equals(lastMonth)) {
                        filteredRecords.add(record);
                    }
                }
                break;
            case LAST_YEAR:
                LocalDate lastYear = LocalDate.now().minusYears(1);
                for (Map<String, Object> record : records) {
                    LocalDate recordDate = (LocalDate) record.get("date");
                    if (recordDate.isAfter(lastYear)) {
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
            String waterBody = (String) record.get("waterBody");
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
            for (Map<String, Object> record : waterBodyRecords) {
                totalFishCaught += (int) record.get("fishCaught");
                if ((double) record.get("biggestFish") > biggestFish) {
                    biggestFish = (double) record.get("biggestFish");
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
}