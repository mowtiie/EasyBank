package com.mowtiie.easybank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mowtiie.easybank.adapters.GenericAdapter;
import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivityProfileBinding;
import com.mowtiie.easybank.databinding.SheetChangePasswordBinding;
import com.mowtiie.easybank.databinding.SheetEditProfileBinding;
import com.mowtiie.easybank.databinding.SheetOpenAccountBinding;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private final ArrayList<String[]> accounts = new ArrayList<>();
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new GenericAdapter<>(accounts, (row, a) -> {
            row.tvTitle.setText(Data.label(a[2]));
            row.tvSubtitle.setText(a[1]);
            row.tvTrailing.setText(Data.money(Integer.parseInt(a[3])));
            row.tvTrailing.setTextColor(MaterialColors.getColor(row.tvTrailing, com.google.android.material.R.attr.colorOnSurface));
        });
        binding.rvAccounts.setAdapter(adapter);

        binding.btnEdit.setOnClickListener(v -> editDetails());
        binding.btnPassword.setOnClickListener(v -> changePassword());
        binding.btnOpenAccount.setOnClickListener(v -> openAccount());
        binding.btnLogout.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", (dialog, which) -> {
                    Data.logout();
                    startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                })
                .setNegativeButton("Cancel", null)
                .show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUser();
        accounts.clear();
        accounts.addAll(Data.accounts(this));
        adapter.notifyDataSetChanged();
    }

    private void showUser() {
        binding.tvName.setText(Data.USER_NAME);
        binding.tvEmail.setText(Data.USER_EMAIL);
        binding.tvPhone.setText(Data.USER_PHONE);
    }

    private void editDetails() {
        SheetEditProfileBinding sheet = SheetEditProfileBinding.inflate(getLayoutInflater());
        sheet.etName.setText(Data.USER_NAME);
        sheet.etEmail.setText(Data.USER_EMAIL);
        sheet.etPhone.setText(Data.USER_PHONE);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(sheet.getRoot());

        sheet.btnSave.setOnClickListener(v -> {
            sheet.tilName.setError(null);
            sheet.tilEmail.setError(null);
            sheet.tilPhone.setError(null);

            String name = String.valueOf(sheet.etName.getText()).trim();
            String email = String.valueOf(sheet.etEmail.getText()).trim();
            String phone = String.valueOf(sheet.etPhone.getText()).trim();

            boolean valid = true;
            if (name.isEmpty()) {
                sheet.tilName.setError("Enter your full name");
                valid = false;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sheet.tilEmail.setError("Enter a valid email");
                valid = false;
            } else if (!email.equalsIgnoreCase(Data.USER_EMAIL) && Data.emailExists(this, email)) {
                sheet.tilEmail.setError("Email is already registered");
                valid = false;
            }
            if (!phone.matches("09\\d{9}")) {
                sheet.tilPhone.setError("Phone must be 11 digits starting with 09");
                valid = false;
            }
            if (!valid) return;

            Data.updateProfile(this, name, email, phone);
            showUser();
            dialog.dismiss();
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void openAccount() {
        SheetOpenAccountBinding sheet = SheetOpenAccountBinding.inflate(getLayoutInflater());
        String[] types = Data.ACCOUNT_TYPES;
        String[] labels = new String[types.length];
        for (int i = 0; i < types.length; i++) labels[i] = Data.label(types[i]);
        int[] selected = {0};
        sheet.ddType.setAdapter(new ArrayAdapter<>(this, com.google.android.material.R.layout.m3_auto_complete_simple_item, labels));
        sheet.ddType.setText(labels[0], false);
        sheet.ddType.setOnItemClickListener((parent, view, position, id) -> selected[0] = position);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(sheet.getRoot());

        sheet.btnOpen.setOnClickListener(v -> {
            String number = Data.openAccount(this, types[selected[0]]);
            accounts.clear();
            accounts.addAll(Data.accounts(this));
            adapter.notifyDataSetChanged();
            dialog.dismiss();
            Toast.makeText(this, labels[selected[0]] + " account " + number + " opened", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void changePassword() {
        SheetChangePasswordBinding sheet = SheetChangePasswordBinding.inflate(getLayoutInflater());
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(sheet.getRoot());

        sheet.btnSave.setOnClickListener(v -> {
            sheet.tilCurrent.setError(null);
            sheet.tilNew.setError(null);
            sheet.tilConfirm.setError(null);

            String current = String.valueOf(sheet.etCurrent.getText());
            String password = String.valueOf(sheet.etNew.getText());
            String confirm = String.valueOf(sheet.etConfirm.getText());

            boolean valid = true;
            if (!Data.passwordMatches(this, current)) {
                sheet.tilCurrent.setError("Current password is wrong");
                valid = false;
            }
            if (password.length() < 6) {
                sheet.tilNew.setError("Password must be at least 6 characters");
                valid = false;
            }
            if (!confirm.equals(password)) {
                sheet.tilConfirm.setError("Passwords do not match");
                valid = false;
            }
            if (!valid) return;

            Data.changePassword(this, password);
            dialog.dismiss();
            Toast.makeText(this, "Password changed", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }
}
