package com.mowtiie.easybank.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowtiie.easybank.databinding.ItemRowBinding;

import java.util.List;

public class GenericAdapter<T> extends RecyclerView.Adapter<GenericAdapter.Holder> {

    public interface Binder<T> {
        void bind(ItemRowBinding row, T item);
    }

    private final List<T> items;
    private final Binder<T> binder;

    public GenericAdapter(List<T> items, Binder<T> binder) {
        this.items = items;
        this.binder = binder;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemRowBinding row;

        Holder(ItemRowBinding row) {
            super(row.getRoot());
            this.row = row;
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        binder.bind(holder.row, items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
