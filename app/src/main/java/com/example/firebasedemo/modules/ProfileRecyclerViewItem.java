package com.example.firebasedemo.modules;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebasedemo.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProfileRecyclerViewItem extends AbstractItem<ProfileRecyclerViewItem.ViewHolder> {

    public String title = null;
    public String description = null;
    public String value = null;
    public int infoIcon = -1;
    public int actionIcon = -1;

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_view_item;
    }

    @NotNull
    @Override
    public ProfileRecyclerViewItem.ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }


    @Override
    public int getType() {
        return R.id.fastadapter_item_adapter;
    }

    static class ViewHolder extends FastAdapter.ViewHolder<ProfileRecyclerViewItem> {

        public TextView title;
        public TextView description;
        public TextView value;
        public ImageView infoIcon, actionIcon;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            value = itemView.findViewById(R.id.value);
            infoIcon = itemView.findViewById(R.id.infoIcon);
            actionIcon = itemView.findViewById(R.id.actionIcon);
        }

        @Override
        public void bindView(@NotNull ProfileRecyclerViewItem item, @NotNull List<?> list) {
            title.setText(item.title);
            description.setText(item.description);
            value.setText(item.value);

            infoIcon.setImageResource(item.infoIcon);
            actionIcon.setImageResource(item.actionIcon);
        }

        @Override
        public void unbindView(@NotNull ProfileRecyclerViewItem item) {
            title.setText(null);
            description.setText(item.description);
            value.setText(item.value);

            infoIcon.setImageResource(item.infoIcon);
            actionIcon.setImageResource(item.actionIcon);
        }
    }
}
