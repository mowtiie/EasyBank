package com.mowtiie.easybank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> {
            binding.tilEmail.setError(null);
            binding.tilPassword.setError(null);

            String email = String.valueOf(binding.etEmail.getText()).trim();
            String password = String.valueOf(binding.etPassword.getText());

            boolean valid = true;
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.setError("Enter a valid email");
                valid = false;
            }
            if (password.isEmpty()) {
                binding.tilPassword.setError("Enter your password");
                valid = false;
            }
            if (!valid) return;

            if (!Data.login(this, email, password)) {
                binding.tilPassword.setError("Wrong email or password");
                return;
            }
            startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        binding.btnForgot.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        binding.btnSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }
}
