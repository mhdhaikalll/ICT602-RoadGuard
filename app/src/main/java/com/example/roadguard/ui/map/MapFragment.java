package com.example.roadguard.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.roadguard.R;
import com.example.roadguard.data.local.entity.CachedReport;
import com.example.roadguard.data.repository.ReportRepository;
import com.example.roadguard.ui.report.ReportFormActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private ClusterManager<CachedReport> clusterManager;
    private MapViewModel viewModel;
    private Map<String, CachedReport> markerReportMap = new HashMap<>();
    private FloatingActionButton fabAddReport;
    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        // Map fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fabAddReport = root.findViewById(R.id.fab_add_report);
        fabAddReport.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ReportFormActivity.class));
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);

        viewModel.getReports().observe(getViewLifecycleOwner(), this::updateMarkers);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // optional progress bar
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Check permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableUserLocationAndCenter();
        } else {
            // Request permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Setup clustering
        clusterManager = new ClusterManager<>(requireContext(), mMap);
        clusterManager.setRenderer(new SeverityClusterRenderer(requireContext(), mMap, clusterManager));
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        clusterManager.setOnClusterItemClickListener(item -> {
            ReportInfoBottomSheet bottomSheet = ReportInfoBottomSheet.newInstance(item);
            bottomSheet.show(getParentFragmentManager(), "report_detail");
            return true;
        });
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocationAndCenter();
            } else {
                // Permission denied – show a message or just leave default camera
            }
        }
    }

    private void enableUserLocationAndCenter() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Show the blue dot
        mMap.setMyLocationEnabled(true);
        ReportRepository.updateUserGeohash(requireContext());
        // Request a fresh location (not just the cached last location)
        com.google.android.gms.location.LocationRequest locationRequest =
                com.google.android.gms.location.LocationRequest.create()
                        .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setNumUpdates(1);               // we only need one fix

        fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        null)   // no cancellation token
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16));
                    } else {
                        // Still null – maybe location services are off?
                        // You could show a toast or a snackbar here

                    }
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
