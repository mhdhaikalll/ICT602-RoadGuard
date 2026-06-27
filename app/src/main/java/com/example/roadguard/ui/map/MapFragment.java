package com.example.roadguard.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.roadguard.R;
import com.example.roadguard.data.local.entity.CachedReport;
import com.example.roadguard.ui.report.ReportFormActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.ClusterManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ClusterManager<CachedReport> clusterManager;
    private MapViewModel viewModel;
    private Map<String, CachedReport> markerReportMap = new HashMap<>();
    private FloatingActionButton fabAddReport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fabAddReport = root.findViewById(R.id.fab_add_report);
        fabAddReport.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ReportFormActivity.class));
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);

        viewModel.getReports().observe(getViewLifecycleOwner(), this::updateMarkers);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Could show a progress bar, for now nothing
        });

        MapDataViewModel mapDataVM = new ViewModelProvider(requireActivity()).get(MapDataViewModel.class);
        mapDataVM.getNotificationLocation().observe(getViewLifecycleOwner(), latLng -> {
            if (mMap != null && latLng != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-6.2, 106.8), 12)); // default

        // Set up clustering
        clusterManager = new ClusterManager<>(requireContext(), mMap);
        clusterManager.setRenderer(new SeverityClusterRenderer(requireContext(), mMap, clusterManager));
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        clusterManager.setOnClusterItemClickListener(item -> {
            // Show bottom sheet
            ReportInfoBottomSheet bottomSheet = ReportInfoBottomSheet.newInstance(item);
            bottomSheet.show(getParentFragmentManager(), "report_detail");
            return true;
        });
    }

    private void updateMarkers(List<CachedReport> reports) {
        if (clusterManager == null || mMap == null) return;
        clusterManager.clearItems();
        clusterManager.addItems(reports);
        clusterManager.cluster();
        markerReportMap.clear();
        for (CachedReport r : reports) {
            markerReportMap.put(r.reportId, r);
        }
    }
}