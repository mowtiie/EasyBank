package com.mowtiie.easybank.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mowtiie.easybank.R;
import com.mowtiie.easybank.adapters.GenericAdapter;
import com.mowtiie.easybank.data.Data;
import com.mowtiie.easybank.databinding.ActivityBillersBinding;

import java.util.ArrayList;

public class BillersActivity extends AppCompatActivity {

    private ActivityBillersBinding binding;
    private final ArrayList<String[]> billers = new ArrayList<>();
    private GenericAdapter<String[]> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBillersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new GenericAdapter<>(billers, (row, b) -> {
            row.tvTitle.setText(b[0]);
            row.tvSubtitle.setText(Data.label(b[1]));
            row.tvTrailing.setText("Pay");
            row.getRoot().setOnClickListener(v -> startActivity(new Intent(this, PayBillActivity.class).putExtra("biller", b[0])));
        });
        binding.rvBillers.setAdapter(adapter);
        binding.chipGroup.setOnCheckedStateChangeListener((group, ids) -> load());
        load();
    }

    private void load() {
        int id = binding.chipGroup.getCheckedChipId();
        String category = null;
        if (id == R.id.chipUtility) category = Data.CATEGORY_UTILITY;
        else if (id == R.id.chipCreditCard) category = Data.CATEGORY_CREDIT_CARD;
        else if (id == R.id.chipOther) category = Data.CATEGORY_OTHER;

        billers.clear();
        for (String[] b : Data.BILLERS) {
            if (category == null || b[1].equals(category)) billers.add(b);
        }
        adapter.notifyDataSetChanged();
    }
}
