package com.scurab.barcodescanner2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scurab.barcodescanner2.forest.ItemLogFood;
import com.scurab.barcodescanner2.forest.ItemdView;
import com.scurab.barcodescanner2.forest.ItemfView;

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

    private List items;
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final NumberFormat mMoneyFormat = NumberFormat.getCurrencyInstance();

    @NonNull
    @Override

    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    //pomocna nastavovacka datumu a casu
    private void setHolderDate(@NonNull ItemViewHolder holder, Date date) {
        if (date!=null) {
            holder.date.setText(mDateFormat.format(date));
            holder.time.setText(mTimeFormat.format(date));
        } else {
            holder.date.setText("");
            holder.time.setText("");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        String msg;
        if (items==null) { //pokud nemame vubec nic
            msg="null"; //tak nic no :-(
        } else { //neco by bylo
            Object oi = items.get(position); //vyzvedneme polozku
            if (oi instanceof ItemdView) { //pokud je do chlast
                ItemdView item = (ItemdView) oi;
                setHolderDate(holder,item.DtCons);
                msg=item.Name; //zaklad je jmeno
                if (item.Year>0) msg+=" "+item.Year; //pokud mame smysluplnej rok, tak to tam dame taky
                if (item.Description!=null) msg+='\n'+item.Description; //pokud je i neco navic, tak prihodime
                holder.description.setText(msg); //no a toto je text polozky
                holder.price.setText(mMoneyFormat.format(item.ConsPrice));
                return;
            } else if (oi instanceof ItemLogFood) { //pokud je to jidlo
                ItemLogFood item = (ItemLogFood) oi;
                setHolderDate(holder,item.DtCons);
                holder.description.setText(holder.itemView.getResources().getString(R.string.logFood)); //tak to dame jako nazev polozky
                holder.price.setText(mMoneyFormat.format(item.ConsPrice));
                return;
            } else { //ostatni neumime,
                msg = oi.toString(); //tak se z toho zkusime vylhat vseobjimajicim tostring
            }
        }
        holder.date.setText(holder.itemView.getResources().getString(R.string.logUnknownItemLine1));
        holder.time.setText(holder.itemView.getResources().getString(R.string.logUnknownItemLine2));
        holder.description.setText(msg);
        holder.price.setText("");
    }

    @Override
    public int getItemCount() {
        return (items==null)?0:items.size(); //kolik toho mame
    }

    public void setItems(List items) {
        this.items=items; //naladujeme polozky
        notifyDataSetChanged(); //a hlasime zmenu, at se s tim nekdo popere
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
}
