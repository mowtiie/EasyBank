package com.mowtiie.easybank.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.color.MaterialColors;
import com.mowtiie.easybank.R;
import com.mowtiie.easybank.adapters.GenericAdapter;
import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivityTransactionsBinding;
import com.mowtiie.easybank.databinding.SheetTransactionBinding;

import java.util.ArrayList;

public class TransactionsActivity extends AppCompatActivity {

    private ActivityTransactionsBinding binding;
    private ArrayList<String[]> accounts = new ArrayList<>();
    private final ArrayList<String[]> transactions = new ArrayList<>();
    private GenericAdapter<String[]> adapter;
    private int selected = 0; // 0 = all accounts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.viewEmpty.tvEmpty.setText("No transactions found");

        // t = {id, type, amount, date, description, counterparty, account number}
        adapter = new GenericAdapter<>(transactions, (row, t) -> {
            boolean debit = Data.isDebit(t[1]);
            row.tvTitle.setText(t[4]);
            row.tvSubtitle.setText(Data.label(t[1]) + " • " + t[3]);
            row.tvTrailing.setText((debit ? "-" : "+") + Data.money(Integer.parseInt(t[2])));
            row.tvTrailing.setTextColor(MaterialColors.getColor(row.tvTrailing, debit ? androidx.appcompat.R.attr.colorError : androidx.appcompat.R.attr.colorPrimary));
            row.getRoot().setOnClickListener(v -> showDetails(t));
        });
        binding.rvTransactions.setAdapter(adapter);

        binding.ddAccount.setOnItemClickListener((parent, view, position, id) -> {
            selected = position;
            load();
        });
        binding.chipGroup.setOnCheckedStateChangeListener((group, ids) -> load());
    }

    @Override
    protected void onResume() {
        super.onResume();
        accounts = Data.accounts(this);
        ArrayList<String> labels = Data.accountLabels(accounts);
        labels.add(0, "All accounts");
        if (selected >= labels.size()) selected = 0;
        binding.ddAccount.setAdapter(new ArrayAdapter<>(this, com.google.android.material.R.layout.m3_auto_complete_simple_item, labels));
        binding.ddAccount.setText(labels.get(selected), false);
        load();
    }

    private void load() {
        int id = binding.chipGroup.getCheckedChipId();
        String[] types = new String[0];
        if (id == R.id.chipDeposit) types = new String[]{Data.TYPE_DEPOSIT};
        else if (id == R.id.chipWithdrawal) types = new String[]{Data.TYPE_WITHDRAWAL};
        else if (id == R.id.chipTransfer) types = new String[]{Data.TYPE_TRANSFER_IN, Data.TYPE_TRANSFER_OUT};
        else if (id == R.id.chipBill) types = new String[]{Data.TYPE_BILL};

        transactions.clear();
        transactions.addAll(Data.transactions(this, selected == 0 ? null : accounts.get(selected - 1)[0], types));
        adapter.notifyDataSetChanged();
        binding.viewEmpty.getRoot().setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showDetails(String[] t) {
        boolean debit = Data.isDebit(t[1]);
        SheetTransactionBinding sheet = SheetTransactionBinding.inflate(getLayoutInflater());
        sheet.tvType.setText(Data.label(t[1]));
        sheet.tvAmount.setText((debit ? "-" : "+") + Data.money(Integer.parseInt(t[2])));
        sheet.tvAmount.setTextColor(MaterialColors.getColor(sheet.tvAmount, debit ? androidx.appcompat.R.attr.colorError : androidx.appcompat.R.attr.colorPrimary));
        sheet.tvDate.setText(t[3]);
        sheet.tvDescription.setText(t[4]);
        sheet.tvAccount.setText(t[6]);
        if (t[5] == null || t[5].isEmpty()) {
            sheet.llRef.setVisibility(View.GONE);
        } else {
            sheet.tvRefLabel.setText(t[1].equals(Data.TYPE_BILL) ? "Reference number" : "Other account number");
            sheet.tvRef.setText(t[5]);
        }
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(sheet.getRoot());
        dialog.show();
    }
}
