package com.mowtiie.easybank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSignUp.setOnClickListener(v -> {
            binding.tilName.setError(null);
            binding.tilEmail.setError(null);
            binding.tilPhone.setError(null);
            binding.tilPassword.setError(null);
            binding.tilConfirm.setError(null);

            String name = String.valueOf(binding.etName.getText()).trim();
            String email = String.valueOf(binding.etEmail.getText()).trim();
            String phone = String.valueOf(binding.etPhone.getText()).trim();
            String password = String.valueOf(binding.etPassword.getText());
            String confirm = String.valueOf(binding.etConfirm.getText());

            boolean valid = true;
            if (name.isEmpty()) {
                binding.tilName.setError("Enter your full name");
                valid = false;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.setError("Enter a valid email");
                valid = false;
            } else if (Data.emailExists(this, email)) {
                binding.tilEmail.setError("Email is already registered");
                valid = false;
            }
            if (!phone.matches("09\\d{9}")) {
                binding.tilPhone.setError("Phone must be 11 digits starting with 09");
                valid = false;
            }
            if (password.length() < 6) {
                binding.tilPassword.setError("Password must be at least 6 characters");
                valid = false;
            }
            if (!confirm.equals(password)) {
                binding.tilConfirm.setError("Passwords do not match");
                valid = false;
            }
            if (!valid) return;

            Data.signUp(this, name, email, phone, password);
            Toast.makeText(this, "Welcome to EasyBank, " + name, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
    }
}
