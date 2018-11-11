package com.scurab.barcodescanner2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by jbruchanov on 09/11/2018.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ItemViewHolder> {

    private final List<ChlastItem> mItems = new ArrayList<>();
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final NumberFormat mMoneyFormat = NumberFormat.getCurrencyInstance();

    @NonNull
    @Override

    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ChlastItem chlastItem = mItems.get(position);
        holder.date.setText(mDateFormat.format(chlastItem.dateTime));
        holder.time.setText(mTimeFormat.format(chlastItem.dateTime));
        holder.description.setText(chlastItem.description);
        holder.price.setText(mMoneyFormat.format(chlastItem.price));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(List<ChlastItem> items) {

        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView time;
        TextView description;
        TextView price;

        public ItemViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.itemDate);
            time = itemView.findViewById(R.id.itemTime);
            description = itemView.findViewById(R.id.itemDescription);
            price = itemView.findViewById(R.id.itemPrice);
        }
    }

    public static class ChlastItem {
        public long dateTime;
        public String description;
        public double price;

        public ChlastItem(long dateTime, String description, double price) {
            this.dateTime = dateTime;
            this.description = description;
            this.price = price;
        }
    }
}
