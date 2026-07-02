package com.example.roadguard.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.roadguard.R;
import com.example.roadguard.data.repository.ReportRepository;
import com.example.roadguard.ui.auth.LoginActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;

    // Views
    private TextView tvDisplayName;
    private TextView tvEmail;
    private Switch switchAlerts;
    private Switch switchTts;
    private ChipGroup chipGroupRadius;
    private Chip chipRadius1;
    private Chip chipRadius3;
    private Chip chipRadius5;
    private Button btnLogout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        tvDisplayName = root.findViewById(R.id.tv_display_name);
        tvEmail = root.findViewById(R.id.tv_email);
        switchAlerts = root.findViewById(R.id.switch_alerts);
        switchTts = root.findViewById(R.id.switch_tts);
        chipGroupRadius = root.findViewById(R.id.chip_group_radius);
        chipRadius1 = root.findViewById(R.id.chip_radius_1);
        chipRadius3 = root.findViewById(R.id.chip_radius_3);
        chipRadius5 = root.findViewById(R.id.chip_radius_5);
        btnLogout = root.findViewById(R.id.btn_logout);
        ReportRepository.updateUserGeohash(requireContext());
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Observe user profile from Room
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                tvDisplayName.setText(profile.displayName);
                tvEmail.setText(profile.email);
                switchAlerts.setChecked(profile.alertsEnabled);
                switchTts.setChecked(profile.ttsEnabled);

                // Select the correct radius chip without triggering listener
                chipGroupRadius.setOnCheckedChangeListener(null);
                switch (profile.notificationRadiusKm) {
                    case 1:
                        chipRadius1.setChecked(true);
                        break;
                    case 3:
                        chipRadius3.setChecked(true);
                        break;
                    case 5:
                        chipRadius5.setChecked(true);
                        break;
                }
                // Reattach the listener
                chipGroupRadius.setOnCheckedChangeListener(radiusChangeListener);
            }
        });

        // Alerts toggle
        switchAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updateAlertsEnabled(isChecked);
        });

        // TTS toggle
        switchTts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updateTtsEnabled(isChecked);
        });

        // Radius selection
        chipGroupRadius.setOnCheckedChangeListener(radiusChangeListener);

        // Logout button
        btnLogout.setOnClickListener(v -> {
            viewModel.logout();
        });

        // Observe logout event
        viewModel.getLogoutEvent().observe(getViewLifecycleOwner(), logout -> {
            if (logout) {
                startActivity(new Intent(requireContext(), LoginActivity.class));
                requireActivity().finish();
            }
        });

        return root;
    }

    // Listener for radius chip changes
    private final ChipGroup.OnCheckedChangeListener radiusChangeListener = (group, checkedId) -> {
        int radius = 3; // default
        if (checkedId == R.id.chip_radius_1) {
            radius = 1;
        } else if (checkedId == R.id.chip_radius_5) {
            radius = 5;
        }
        viewModel.updateNotificationRadius(radius);
    };
}