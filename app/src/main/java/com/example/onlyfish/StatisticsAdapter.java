package com.example.onlyfish;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder> {

    private List<Map<String, Object>> statisticsList;

    public StatisticsAdapter(List<Map<String, Object>> statisticsList) {
        this.statisticsList = statisticsList;
    }

    public void updateData(List<Map<String, Object>> newData) {
        statisticsList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatisticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.statistics_item, parent, false);
        return new StatisticsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticsViewHolder holder, int position) {
        Map<String, Object> statistics = statisticsList.get(position);
        holder.waterBodyTextView.setText(String.valueOf(statistics.get("waterBody")));
        holder.averageFishCaughtTextView.setText(String.format("%.2f", statistics.get("averageFishCaught")));
        holder.biggestFishTextView.setText(String.format("%.2f", statistics.get("biggestFish")));
        holder.totalFishCaughtTextView.setText(String.valueOf(statistics.get("totalFishCaught")));
        holder.numberOfRecordsTextView.setText(String.valueOf(statistics.get("numberOfRecords")));
    }

    @Override
    public int getItemCount() {
        return statisticsList.size();
    }

    public static class StatisticsViewHolder extends RecyclerView.ViewHolder {
        public TextView waterBodyTextView;
        public TextView averageFishCaughtTextView;
        public TextView biggestFishTextView;
        public TextView totalFishCaughtTextView;
        public TextView numberOfRecordsTextView;

        public StatisticsViewHolder(View itemView) {
            super(itemView);
            waterBodyTextView = itemView.findViewById(R.id.waterBodyTextView);
            averageFishCaughtTextView = itemView.findViewById(R.id.averageFishCaughtTextView);
            biggestFishTextView = itemView.findViewById(R.id.biggestFishTextView);
            totalFishCaughtTextView = itemView.findViewById(R.id.totalFishCaughtTextView);
            numberOfRecordsTextView = itemView.findViewById(R.id.numberOfRecordsTextView);
        }
    }
}