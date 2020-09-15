package com.example.firebasedemo.modules;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebasedemo.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

public class WhoIMetRecyclerViewItem extends AbstractItem<WhoIMetRecyclerViewItem.ViewHolder> {

    public String name = null;
    public String phone = null;
    public String date = null;
    public int profilePic = -1;
    public DataHolder dataHolder = null;

    public static class DataHolder implements Serializable {
        public String name, phone, date, id;

        public DataHolder(String name, String phone, String date, String id) {
            this.name = name;
            this.phone = phone;
            this.date = date;
            this.id = id;
        }
    }

    @Override
    public int getLayoutRes() {
        return R.layout.who_i_met_recycler_view_item;
    }

    @NotNull
    @Override
    public WhoIMetRecyclerViewItem.ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item_adapter;
    }

    static class ViewHolder extends FastAdapter.ViewHolder<WhoIMetRecyclerViewItem> {

        public TextView name;
        public TextView phone;
        public TextView date;
        public ImageView profilePic;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.personName);
            phone = itemView.findViewById(R.id.personPhone);
            date = itemView.findViewById(R.id.personDate);

            profilePic = itemView.findViewById(R.id.personImageView);
        }

        @Override
        public void bindView(@NotNull WhoIMetRecyclerViewItem item, @NotNull List<?> list) {
            name.setText(item.name);
            phone.setText(item.phone);
            date.setText(item.date);

            profilePic.setImageResource(item.profilePic);
        }

        @Override
        public void unbindView(@NotNull WhoIMetRecyclerViewItem item) {
            name.setText(null);
            phone.setText(item.phone);
            date.setText(item.date);

            profilePic.setImageResource(item.profilePic);
        }
    }
}
