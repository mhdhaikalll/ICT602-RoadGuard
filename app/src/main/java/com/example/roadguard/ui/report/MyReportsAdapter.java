package com.example.roadguard.ui.report;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roadguard.R;
import com.example.roadguard.data.local.entity.CachedReport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyReportsAdapter extends RecyclerView.Adapter<MyReportsAdapter.ViewHolder> {
    private List<CachedReport> reports;

    public MyReportsAdapter(List<CachedReport> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CachedReport report = reports.get(position);
        holder.tvSeverity.setText("Severity: " + report.severity);
        holder.tvNotes.setText(report.notes.isEmpty() ? "No notes" : report.notes);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
        holder.tvTimestamp.setText(sdf.format(new Date(report.timestamp)));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditReportActivity.class);
            intent.putExtra("report", report);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }

    public void updateList(List<CachedReport> newList) {
        reports = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeverity, tvNotes, tvTimestamp;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeverity = itemView.findViewById(R.id.tv_item_severity);
            tvNotes = itemView.findViewById(R.id.tv_item_notes);
            tvTimestamp = itemView.findViewById(R.id.tv_item_timestamp);
        }
    }
}
