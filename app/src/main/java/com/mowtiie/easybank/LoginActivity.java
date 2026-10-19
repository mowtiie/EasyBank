package com.mowtiie.easybank;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mowtiie.easybank.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    SessionManager sessionManager;
    EasyBankDatabase easyBankDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        easyBankDatabase = new EasyBankDatabase(this);
        sessionManager = new SessionManager(this);

        binding.btnCreateOne.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        binding.btnLogin.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.emailInput.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.passwordInput.getText()).toString().trim();

            int userId = easyBankDatabase.login(email, password);
            if (userId == -1) {
                Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show();
            } else {
                sessionManager.setUserId(userId);
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }
        });
    }
}