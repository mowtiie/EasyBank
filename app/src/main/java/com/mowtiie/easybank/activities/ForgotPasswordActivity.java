package com.mowtiie.easybank.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnReset.setOnClickListener(v -> {
            binding.tilEmail.setError(null);
            binding.tilPassword.setError(null);
            binding.tilConfirm.setError(null);

            String email = String.valueOf(binding.etEmail.getText()).trim();
            String password = String.valueOf(binding.etPassword.getText());
            String confirm = String.valueOf(binding.etConfirm.getText());

            boolean valid = true;
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.setError("Enter a valid email");
                valid = false;
            } else if (!Data.emailExists(this, email)) {
                binding.tilEmail.setError("No account with this email");
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

            Data.resetPassword(this, email, password);
            Toast.makeText(this, "Password reset. Log in with your new password.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
