package com.example.tellymobile;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageActivity extends BaseActivity {

    private RecyclerView rvNotifications;
    private DatabaseHelper databaseHelper;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        
        databaseHelper = new DatabaseHelper(this);
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);

        loadNotifications();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Mark All Read").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) {
            databaseHelper.markAllNotificationsAsRead();
            loadNotifications();
            NotificationUtils.showTopNotification(this, databaseHelper, "All marked as read", false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNotifications() {
        notificationList.clear();
        Cursor cursor = databaseHelper.getAllNotifications();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
                boolean isRead = cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1;

                notificationList.add(new Notification(id, title, message, type, timestamp, isRead));
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
        
        // Also mark visible ones as read? Or leave it to user click? 
        // For now, let's keep them unread until clicked or "Mark All Read"
    }

    // Inner Class for Model
    private static class Notification {
        int id;
        String title, message, type;
        long timestamp;
        boolean isRead;

        public Notification(int id, String title, String message, String type, long timestamp, boolean isRead) {
            this.id = id; this.title = title; this.message = message; this.type = type; this.timestamp = timestamp; this.isRead = isRead;
        }
    }

    // Inner Class for Adapter
    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        
        private List<Notification> list;

        public NotificationAdapter(List<Notification> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notification item = list.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvMessage.setText(item.message);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(item.timestamp)));

            // Visual for Read/Unread
            if (item.isRead) {
                holder.tvTitle.setTypeface(null, android.graphics.Typeface.NORMAL);
                holder.card.setCardElevation(2); // Lower elevation
                holder.card.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(MessageActivity.this, android.R.color.white));
            } else {
                holder.tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                holder.card.setCardElevation(8); // Higher elevation
                holder.card.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(MessageActivity.this, android.R.color.white)); 
            }
            
            holder.itemView.setOnClickListener(v -> {
                if (!item.isRead) {
                    databaseHelper.markNotificationAsRead(item.id);
                    item.isRead = true;
                    notifyItemChanged(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMessage, tvDate;
            MaterialCardView card;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvDate = itemView.findViewById(R.id.tvDate);
                card = itemView.findViewById(R.id.cardNotification);
            }
        }
    }
}
