package com.mowtiie.easybank.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivityCashBinding;

import java.util.ArrayList;

public class CashActivity extends AppCompatActivity {

    private ActivityCashBinding binding;
    private ArrayList<String[]> accounts = new ArrayList<>();
    private int selected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tilAmount.setPrefixText(Data.CURRENCY);
        binding.ddAccount.setOnItemClickListener((parent, view, position, id) -> selected = position);
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.tilAmount.setError(null);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        binding.btnConfirm.setOnClickListener(v -> confirm());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAccounts();
    }

    private void loadAccounts() {
        accounts = Data.accounts(this);
        if (selected >= accounts.size()) selected = 0;
        ArrayList<String> labels = Data.accountLabels(accounts);
        binding.ddAccount.setAdapter(new ArrayAdapter<>(this, com.google.android.material.R.layout.m3_auto_complete_simple_item, labels));
        binding.ddAccount.setText(labels.get(selected), false);
    }

    private void confirm() {
        binding.tilAmount.setError(null);
        String text = String.valueOf(binding.etAmount.getText()).trim();
        if (text.isEmpty()) {
            binding.tilAmount.setError("Enter an amount");
            return;
        }
        int amount = Integer.parseInt(text);
        if (amount < 1 || amount > Data.MAX_AMOUNT) {
            binding.tilAmount.setError("Amount must be " + Data.money(1) + " to " + Data.money(Data.MAX_AMOUNT));
            return;
        }

        String[] account = accounts.get(selected);
        boolean deposit = binding.tabs.getSelectedTabPosition() == 0;
        if (!deposit && amount > Integer.parseInt(account[3])) {
            binding.tilAmount.setError("Insufficient funds");
            Data.failure(this, "Withdrawal failed", "Insufficient funds in account " + account[1] + ".");
            return;
        }

        binding.viewProcessing.getRoot().setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Data.cash(this, account[0], deposit, amount);
            binding.viewProcessing.getRoot().setVisibility(View.GONE);
            binding.etAmount.setText("");
            loadAccounts();
            Toast.makeText(this, (deposit ? "Deposited " : "Withdrew ") + Data.money(amount), Toast.LENGTH_SHORT).show();
        }, 1000);
    }
}
