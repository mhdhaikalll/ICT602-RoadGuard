package com.example.roadguard.ui.map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.roadguard.R;
import com.example.roadguard.data.local.entity.CachedReport;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ReportInfoBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_REPORT = "report";
    private CachedReport report;

    public static ReportInfoBottomSheet newInstance(CachedReport report) {
        ReportInfoBottomSheet fragment = new ReportInfoBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REPORT, report);  // CachedReport must implement Serializable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            report = (CachedReport) getArguments().getSerializable(ARG_REPORT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_report_detail, container, false);

        TextView tvSeverity = view.findViewById(R.id.tv_severity);
        TextView tvDistance = view.findViewById(R.id.tv_distance);
        TextView tvTime = view.findViewById(R.id.tv_time);
        TextView tvNotes = view.findViewById(R.id.tv_notes);
        Button btnUpvote = view.findViewById(R.id.btn_upvote);
        Button btnDownvote = view.findViewById(R.id.btn_downvote);
        Button btnNavigate = view.findViewById(R.id.btn_navigate);
        TextView tvVotes = view.findViewById(R.id.tv_votes);

        tvSeverity.setText("Severity: " + report.severity.toUpperCase());
        tvNotes.setText(report.notes.isEmpty() ? "No notes" : report.notes);

        // Time since reported
        long now = System.currentTimeMillis();
        long diff = now - report.timestamp;
        String timeAgo = getTimeAgo(diff);
        tvTime.setText("Reported: " + timeAgo);

        // Distance (placeholder – you need user location; we can show "calculating...")
        tvDistance.setText("Distance: calculating...");

        // Votes
        tvVotes.setText("👍 " + report.upvotes + "  👎 " + report.downvotes);

        // Upvote / Downvote (to be implemented later)
        btnUpvote.setOnClickListener(v -> { /* TODO */ });
        btnDownvote.setOnClickListener(v -> { /* TODO */ });

        // Navigate button opens Google Maps
        btnNavigate.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + report.latitude + "," + report.longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });

        return view;
    }

    private String getTimeAgo(long millis) {
        // Simple implementation; you can improve later
        long minutes = millis / (1000 * 60);
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + "d ago";
        if (hours > 0) return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        return "just now";
    }
}
