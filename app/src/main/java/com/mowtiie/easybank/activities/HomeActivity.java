package com.mowtiie.easybank.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.MaterialColors;
import com.mowtiie.easybank.R;
import com.mowtiie.easybank.adapters.GenericAdapter;
import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivityHomeBinding;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private final ArrayList<String[]> accounts = new ArrayList<>();
    private final ArrayList<String[]> recent = new ArrayList<>();
    private GenericAdapter<String[]> accountAdapter;
    private GenericAdapter<String[]> recentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Data.USER_* is lost if the process was killed, so go back to Login.
        if (Data.USER_ID == 0) {
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return;
        }
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvGreeting.setText("Hello, " + Data.USER_NAME);
        binding.viewEmptyRecent.tvEmpty.setText("No transactions yet");

        accountAdapter = new GenericAdapter<>(accounts, (row, a) -> {
            row.tvTitle.setText(Data.label(a[2]));
            row.tvSubtitle.setText(a[1]);
            row.tvTrailing.setText(Data.money(Integer.parseInt(a[3])));
            row.tvTrailing.setTextColor(MaterialColors.getColor(row.tvTrailing, com.google.android.material.R.attr.colorOnSurface));
        });
        binding.rvAccounts.setAdapter(accountAdapter);

        recentAdapter = new GenericAdapter<>(recent, (row, t) -> {
            boolean debit = Data.isDebit(t[1]);
            row.tvTitle.setText(t[4]);
            row.tvSubtitle.setText(Data.label(t[1]) + " • " + t[3]);
            row.tvTrailing.setText((debit ? "-" : "+") + Data.money(Integer.parseInt(t[2])));
            row.tvTrailing.setTextColor(MaterialColors.getColor(row.tvTrailing, debit ? androidx.appcompat.R.attr.colorError : androidx.appcompat.R.attr.colorPrimary));
        });
        binding.rvRecent.setAdapter(recentAdapter);

        binding.toolbar.setOnMenuItemClickListener(item -> {
            startActivity(new Intent(this, item.getItemId() == R.id.menuNotifications ? NotificationsActivity.class : ProfileActivity.class));
            return true;
        });
        binding.btnCash.setOnClickListener(v -> startActivity(new Intent(this, CashActivity.class)));
        binding.btnTransfer.setOnClickListener(v -> startActivity(new Intent(this, TransferActivity.class)));
        binding.btnBills.setOnClickListener(v -> startActivity(new Intent(this, BillersActivity.class)));
        binding.btnHistory.setOnClickListener(v -> startActivity(new Intent(this, TransactionsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding == null) return;

        accounts.clear();
        accounts.addAll(Data.accounts(this));
        accountAdapter.notifyDataSetChanged();

        recent.clear();
        recent.addAll(Data.recentTransactions(this, 5));
        recentAdapter.notifyDataSetChanged();
        binding.viewEmptyRecent.getRoot().setVisibility(recent.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);

        binding.tvTotal.setText(Data.money(Data.totalBalance(this)));

        int unread = Data.unreadCount(this);
        binding.toolbar.getMenu().findItem(R.id.menuNotifications).setTitle(unread > 0 ? "Notifications (" + unread + ")" : "Notifications");
    }
}
