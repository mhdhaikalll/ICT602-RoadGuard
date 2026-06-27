package com.example.roadguard.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roadguard.R;

import java.util.ArrayList;

public class MyReportsFragment extends Fragment {
    private RecyclerView recyclerView;
    private MyReportsAdapter adapter;
    private MyReportsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_reports, container, false);
        recyclerView = root.findViewById(R.id.rv_my_reports);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyReportsAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MyReportsViewModel.class);
        viewModel.getUserReports().observe(getViewLifecycleOwner(), reports -> {
            adapter.updateList(reports);
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            // You can show a Toast or TextView
        });
        return root;
    }
}