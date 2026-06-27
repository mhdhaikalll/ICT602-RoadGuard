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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.roadguard.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ReportFormActivity extends AppCompatActivity {
    private ReportViewModel viewModel;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private Location selectedLocation;

    // UI elements
    private RadioGroup radioSeverity;
    private RadioButton radioLow, radioMedium, radioHigh;
    private EditText etNotes;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        radioSeverity = findViewById(R.id.radio_severity);
        radioLow = findViewById(R.id.radio_low);
        radioMedium = findViewById(R.id.radio_medium);
        radioHigh = findViewById(R.id.radio_high);
        etNotes = findViewById(R.id.et_notes);
        btnSubmit = findViewById(R.id.btn_submit);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_picker);
        mapFragment.getMapAsync(this);

        btnSubmit.setOnClickListener(v -> {
            String severity = "low";
            int checkedId = radioSeverity.getCheckedRadioButtonId();
            if (checkedId == R.id.radio_medium) severity = "medium";
            else if (checkedId == R.id.radio_high) severity = "high";

            String notes = etNotes.getText().toString().trim();
            viewModel.submitReport(severity, notes);
        });

        viewModel.getSubmitSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Report submitted!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    selectedLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                    map.addMarker(new MarkerOptions().position(latLng).title("Hazard here"));
                }
            });
        }

        // Allow user to move the marker
        map.setOnMapClickListener(latLng -> {
            map.clear();
            map.addMarker(new MarkerOptions().position(latLng).title("Hazard here"));
            selectedLocation = new Location("");
            selectedLocation.setLatitude(latLng.latitude);
            selectedLocation.setLongitude(latLng.longitude);
            viewModel.setLocation(selectedLocation);
        });
    }
}