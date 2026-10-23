package com.example.smartpantrymanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Shows the pantry items in a RecyclerView, one row per ingredient. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean highlightExpiring = true;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setHighlightExpiring(boolean highlight) {
        this.highlightExpiring = highlight;
    }

    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PantryItem item = items.get(position);
        h.tvName.setText(item.name);
        h.tvDetails.setText(Fmt.qty(item.quantity) + " " + item.unit);

        if (item.expiry == null || item.expiry.isEmpty()) {
            h.tvExpiry.setText("");
        } else {
            h.tvExpiry.setText("Expires: " + item.expiry);
        }
        boolean soon = highlightExpiring && expiringSoon(item.expiry);
        h.tvExpiry.setTextColor(soon ? Color.RED : Color.GRAY);

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** True if the date is within 3 days from now (or already past). */
    private boolean expiringSoon(String date) {
        if (date == null || date.isEmpty()) {
            return false;
        }
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
            if (d == null) {
                return false;
            }
            long days = (d.getTime() - System.currentTimeMillis()) / (1000L * 60 * 60 * 24);
            return days <= 3;
        } catch (ParseException e) {
            return false;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvDetails;
        final TextView tvExpiry;
        final Button btnDelete;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvDetails = v.findViewById(R.id.tvDetails);
            tvExpiry = v.findViewById(R.id.tvExpiry);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
