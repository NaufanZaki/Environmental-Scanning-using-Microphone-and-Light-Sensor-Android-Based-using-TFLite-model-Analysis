package com.example.fpmobile;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fpmobile.database.ScanEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<ScanEntity> scanList;

    public HistoryAdapter(List<ScanEntity> scanList) {
        this.scanList = scanList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ScanEntity scan = scanList.get(position);

        holder.decibelsTextView.setText(String.format(Locale.getDefault(), "Decibels: %.2f dB", scan.getDecibels()));
        holder.luxTextView.setText(String.format(Locale.getDefault(), "Lux: %.2f", scan.getLux()));
        holder.recommendationTextView.setText(String.format(Locale.getDefault(), "Recommendation: %s", scan.getRecommendation()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.timestampTextView.setText("Time: " + sdf.format(scan.getTimestamp()));

        // Set listener for View Details button
        holder.viewDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ScanDetailActivity.class);
            intent.putExtra("scanId", scan.getId());  // Pass scanId to ScanDetailActivity
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return scanList != null ? scanList.size() : 0;
    }

    // ViewHolder for each item in the RecyclerView
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView decibelsTextView, luxTextView, recommendationTextView, timestampTextView;
        Button viewDetailsButton;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            decibelsTextView = itemView.findViewById(R.id.decibelsTextView);
            luxTextView = itemView.findViewById(R.id.luxTextView);
            recommendationTextView = itemView.findViewById(R.id.recommendationTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}

