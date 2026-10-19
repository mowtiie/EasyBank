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

import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivityPayBillBinding;

import java.util.ArrayList;

public class PayBillActivity extends AppCompatActivity {

    private ActivityPayBillBinding binding;
    private ArrayList<String[]> accounts = new ArrayList<>();
    private int selected = 0;
    private String biller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPayBillBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        biller = getIntent().getStringExtra("biller");
        binding.toolbar.setTitle("Pay " + biller);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tilAmount.setPrefixText(Data.CURRENCY);
        binding.ddAccount.setOnItemClickListener((parent, view, position, id) -> selected = position);
        binding.btnPay.setOnClickListener(v -> pay());
        binding.viewResult.btnDone.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        accounts = Data.accounts(this);
        if (selected >= accounts.size()) selected = 0;
        ArrayList<String> labels = Data.accountLabels(accounts);
        binding.ddAccount.setAdapter(new ArrayAdapter<>(this, com.google.android.material.R.layout.m3_auto_complete_simple_item, labels));
        binding.ddAccount.setText(labels.get(selected), false);
    }

    private void pay() {
        binding.tilReference.setError(null);
        binding.tilAmount.setError(null);

        String reference = String.valueOf(binding.etReference.getText()).trim();
        String text = String.valueOf(binding.etAmount.getText()).trim();

        boolean valid = true;
        if (!reference.matches("\\d{6,12}")) {
            binding.tilReference.setError("Enter 6 to 12 digits");
            valid = false;
        }
        if (text.isEmpty()) {
            binding.tilAmount.setError("Enter an amount");
            valid = false;
        } else if (Integer.parseInt(text) < 1 || Integer.parseInt(text) > Data.MAX_AMOUNT) {
            binding.tilAmount.setError("Amount must be " + Data.money(1) + " to " + Data.money(Data.MAX_AMOUNT));
            valid = false;
        }
        if (!valid) return;

        int amount = Integer.parseInt(text);
        String[] account = accounts.get(selected);
        if (amount > Integer.parseInt(account[3])) {
            binding.tilAmount.setError("Insufficient funds");
            Data.failure(this, "Bill payment failed", "Insufficient funds in account " + account[1] + " to pay " + biller + ".");
            return;
        }

        binding.viewProcessing.getRoot().setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Data.payBill(this, account[0], biller, reference, amount);
            binding.viewProcessing.getRoot().setVisibility(View.GONE);
            binding.svForm.setVisibility(View.GONE);
            binding.viewResult.getRoot().setVisibility(View.VISIBLE);
            binding.viewResult.tvResultTitle.setText("Payment successful");
            binding.viewResult.tvResultMessage.setText(Data.money(amount) + " was paid to " + biller + ".");
            Toast.makeText(this, "Bill paid: " + biller, Toast.LENGTH_SHORT).show();
        }, 1000);
    }
}
