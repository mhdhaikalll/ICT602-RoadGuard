package com.example.roadguard.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.roadguard.MainActivity;
import com.example.roadguard.R;

public class RegisterActivity extends AppCompatActivity {
    private AuthViewModel viewModel;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSubmit;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        etName = (String) findViewById(R.id.et_name);
        etEmail = (String) findViewById(R.id.et_email);
        etPassword = (String) findViewById(R.id.et_password);
        btnSubmit = (Button) findViewById(R.id.btn_submit);
        tvLoginLink = (TextView) findViewById(R.id.tv_login_link);

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
            viewModel.register(email, password, name);
        });
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        viewModel.getLoginSuccess().observe(this, success -> {
            if (success) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}