package com.mowtiie.easybank.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.MaterialColors;
import com.mowtiie.easybank.adapters.GenericAdapter;
import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivityNotificationsBinding;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private final ArrayList<String[]> notifications = new ArrayList<>();
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.viewEmpty.tvEmpty.setText("No notifications yet");

        // n = {id, title, message, date, is_read}
        adapter = new GenericAdapter<>(notifications, (row, n) -> {
            boolean unread = n[4].equals("0");
            row.tvTitle.setText(n[1]);
            row.tvTitle.setTypeface(null, unread ? Typeface.BOLD : Typeface.NORMAL);
            row.tvSubtitle.setText(n[2] + "\n" + n[3]);
            row.tvTrailing.setText(unread ? "New" : "");
            row.tvTrailing.setTextColor(MaterialColors.getColor(row.tvTrailing, androidx.appcompat.R.attr.colorPrimary));
        });
        binding.rvNotifications.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifications.clear();
        notifications.addAll(Data.notifications(this));
        adapter.notifyDataSetChanged();
        binding.viewEmpty.getRoot().setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
        // The list above still shows the unread marks for this visit.
        Data.markNotificationsRead(this);
    }
}
