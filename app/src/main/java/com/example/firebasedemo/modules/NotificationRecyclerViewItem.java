package com.example.firebasedemo.modules;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebasedemo.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NotificationRecyclerViewItem extends AbstractItem<NotificationRecyclerViewItem.ViewHolder> {

    public String message = null;
    public Long date = null;
    public Boolean isRead = null;
    public int notificationIcon = -1;

    @Override
    public int getLayoutRes() {
        return R.layout.notification_view_item;
    }

    @NotNull
    @Override
    public NotificationRecyclerViewItem.ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }


    @Override
    public int getType() {
        return R.id.fastadapter_item_adapter;
    }

    static class ViewHolder extends FastAdapter.ViewHolder<NotificationRecyclerViewItem> {

        public TextView message;
        public TextView date;
        public ImageView notificationsIcon;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.notificationMessage);
            date = itemView.findViewById(R.id.notificationDate);
            notificationsIcon = itemView.findViewById(R.id.notificationImageView);

        }

        @Override
        public void bindView(@NotNull NotificationRecyclerViewItem item, @NotNull List<?> list) {
            // TODO when isRead is false change the color of the notification
            message.setText(item.message);
            // TODO transform the long date into a valid date with days and  months and hours
            date.setText(String.valueOf(item.date));
            // end TODO
            notificationsIcon.setImageResource(item.notificationIcon);
        }

        @Override
        public void unbindView(@NotNull NotificationRecyclerViewItem item) {
            message.setText(null);
            date.setText(null);
            notificationsIcon.setImageResource(item.notificationIcon);
        }
    }
}
