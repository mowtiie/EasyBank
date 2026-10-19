package com.mowtiie.easybank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivityTransferBinding;

import java.util.ArrayList;

public class TransferActivity extends AppCompatActivity {

    private ActivityTransferBinding binding;
    private ArrayList<String[]> accounts = new ArrayList<>();
    private int selectedFrom = 0;
    private int selectedTo = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tilAmount.setPrefixText(Data.CURRENCY);
        binding.ddFrom.setOnItemClickListener((parent, view, position, id) -> selectedFrom = position);
        binding.ddTo.setOnItemClickListener((parent, view, position, id) -> selectedTo = position);
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean own = tab.getPosition() == 0;
                binding.tilTo.setVisibility(own ? View.VISIBLE : View.GONE);
                binding.tilNumber.setVisibility(own ? View.GONE : View.VISIBLE);
                binding.tilTo.setError(null);
                binding.tilNumber.setError(null);
                binding.tilAmount.setError(null);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        binding.btnTransfer.setOnClickListener(v -> transfer());
        binding.viewResult.btnDone.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        accounts = Data.accounts(this);
        if (selectedFrom >= accounts.size()) selectedFrom = 0;
        if (selectedTo >= accounts.size()) selectedTo = 0;
        ArrayList<String> labels = Data.accountLabels(accounts);
        binding.ddFrom.setAdapter(new ArrayAdapter<>(this, com.google.android.material.R.layout.m3_auto_complete_simple_item, labels));
        binding.ddTo.setAdapter(new ArrayAdapter<>(this, com.google.android.material.R.layout.m3_auto_complete_simple_item, labels));
        binding.ddFrom.setText(labels.get(selectedFrom), false);
        binding.ddTo.setText(labels.get(selectedTo), false);
    }

    private void transfer() {
        binding.tilTo.setError(null);
        binding.tilNumber.setError(null);
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

        String[] from = accounts.get(selectedFrom);
        boolean own = binding.tabs.getSelectedTabPosition() == 0;
        String[] to;
        if (own) {
            to = accounts.get(selectedTo);
            if (to[0].equals(from[0])) {
                binding.tilTo.setError("Choose a different account");
                return;
            }
        } else {
            String number = String.valueOf(binding.etNumber.getText()).trim();
            if (!number.matches("\\d{10}")) {
                binding.tilNumber.setError("Enter a 10-digit account number");
                return;
            }
            to = Data.accountByNumber(this, number);
            if (to == null) {
                binding.tilNumber.setError("Account number not found");
                Data.failure(this, "Transfer failed", "Account number " + number + " does not exist.");
                return;
            }
            if (to[1].equals(String.valueOf(Data.USER_ID))) {
                binding.tilNumber.setError("That is one of your own accounts");
                return;
            }
        }
        if (amount > Integer.parseInt(from[3])) {
            binding.tilAmount.setError("Insufficient funds");
            Data.failure(this, "Transfer failed", "Insufficient funds in account " + from[1] + ".");
            return;
        }

        String toId = to[0];
        String recipient = own ? "your " + Data.label(to[2]) + " account" : to[4] + " (" + to[2] + ")";
        if (own) {
            send(from[0], toId, amount, recipient);
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Confirm transfer")
                    .setMessage("Send " + Data.money(amount) + " to " + to[4] + " (" + to[2] + ")?")
                    .setPositiveButton("Send", (dialog, which) -> send(from[0], toId, amount, recipient))
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void send(String fromId, String toId, int amount, String recipient) {
        binding.viewProcessing.getRoot().setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Data.transfer(this, fromId, toId, amount);
            binding.viewProcessing.getRoot().setVisibility(View.GONE);
            binding.llForm.setVisibility(View.GONE);
            binding.viewResult.getRoot().setVisibility(View.VISIBLE);
            binding.viewResult.tvResultTitle.setText("Transfer successful");
            binding.viewResult.tvResultMessage.setText(Data.money(amount) + " was sent to " + recipient + ".");
            Toast.makeText(this, "Transfer sent: " + Data.money(amount), Toast.LENGTH_SHORT).show();
        }, 1000);
    }
}
