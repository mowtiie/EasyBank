package com.mowtiie.easybank;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mowtiie.easybank.databinding.ActivityRegisterBinding;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    SessionManager sessionManager;
    EasyBankDatabase easyBankDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        easyBankDatabase = new EasyBankDatabase(this);
        sessionManager = new SessionManager(this);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.btnRegister.setOnClickListener(v -> {
            String fullName = Objects.requireNonNull(binding.fullNameInput.getText()).toString().trim();
            String phone = Objects.requireNonNull(binding.phoneInput.getText()).toString().trim();
            String email = Objects.requireNonNull(binding.emailInput.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.passwordInput.getText()).toString().trim();
            String confirmPassword = Objects.requireNonNull(binding.confirmPasswordInput.getText()).toString().trim();

            if (!password.equalsIgnoreCase(confirmPassword)) {
                Toast.makeText(this, "Passwords must match", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User();
            user.fullName = fullName;
            user.phone = phone;
            user.email = email;
            user.password = password;

            if (easyBankDatabase.register(user)) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}