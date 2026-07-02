package com.example.roadguard.ui.report;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.roadguard.R;
import com.example.roadguard.data.local.entity.CachedReport;
import com.example.roadguard.data.repository.ReportRepository;
import com.example.roadguard.util.GeohashHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class EditReportActivity extends AppCompatActivity implements OnMapReadyCallback {
    private CachedReport report;
    private GoogleMap map;
    private Location selectedLocation;
    private RadioGroup radioSeverity;
    private RadioButton radioLow, radioMedium, radioHigh;
    private EditText etNotes;
    private Button btnUpdate, btnDelete;
    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_report);

        report = (CachedReport) getIntent().getSerializableExtra("report");
        if (report == null) {
            finish();
            return;
        }

        reportRepository = new ReportRepository(getApplication());

        radioSeverity = findViewById(R.id.radio_severity);
        radioLow = findViewById(R.id.radio_low);
        radioMedium = findViewById(R.id.radio_medium);
        radioHigh = findViewById(R.id.radio_high);
        etNotes = findViewById(R.id.et_notes);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);

        // Pre-fill fields
        switch (report.severity) {
            case "low":    radioLow.setChecked(true); break;
            case "medium": radioMedium.setChecked(true); break;
            case "high":   radioHigh.setChecked(true); break;
        }
        etNotes.setText(report.notes);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_picker);
        mapFragment.getMapAsync(this);

        btnUpdate.setOnClickListener(v -> {
            String severity = "low";
            int checkedId = radioSeverity.getCheckedRadioButtonId();
            if (checkedId == R.id.radio_medium) severity = "medium";
            else if (checkedId == R.id.radio_high) severity = "high";

            String notes = etNotes.getText().toString().trim();

            Map<String, Object> updates = new HashMap<>();
            updates.put("severity", severity);
            updates.put("notes", notes);

            if (selectedLocation != null) {
                updates.put("latitude", selectedLocation.getLatitude());
                updates.put("longitude", selectedLocation.getLongitude());
                // optionally update geohash
                String geohash = GeohashHelper.encode(
                        selectedLocation.getLatitude(), selectedLocation.getLongitude(), 9);
                updates.put("geohash", geohash);
            }

            reportRepository.updateReport(report.reportId, updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Report updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Report")
                    .setMessage("Are you sure you want to delete this report?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        reportRepository.deleteReport(report.reportId)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show();
                                    finish(); // go back to the list
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        LatLng originalPos = new LatLng(report.latitude, report.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(originalPos, 18));
        map.addMarker(new MarkerOptions().position(originalPos).title("Hazard location"));
        selectedLocation = new Location("");
        selectedLocation.setLatitude(report.latitude);
        selectedLocation.setLongitude(report.longitude);

        map.setOnMapClickListener(latLng -> {
            map.clear();
            map.addMarker(new MarkerOptions().position(latLng).title("New location"));
            selectedLocation.setLatitude(latLng.latitude);
            selectedLocation.setLongitude(latLng.longitude);
        });
    }
}