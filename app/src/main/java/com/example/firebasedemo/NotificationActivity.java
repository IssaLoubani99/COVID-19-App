package com.example.firebasedemo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasedemo.modules.Notification;
import com.example.firebasedemo.modules.NotificationRecyclerViewItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

public class NotificationActivity extends AppCompatActivity {
    // firebase init
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    // ref
    CollectionReference whoIMetRef;
    Toolbar notificationToolbar;
    // components
    RecyclerView notificationToolBar;
    ItemAdapter<NotificationRecyclerViewItem> itemAdapter;
    FastAdapter<NotificationRecyclerViewItem> fastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initFirebase();
        initComponents();
        initRecyclerViewAdapters();
        setSupportActionBar(notificationToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fetchData();
    }


    private void fetchData() {
        whoIMetRef.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                Log.w("fetching data@", "status:success");
                task.getResult().getDocuments().forEach(documentSnapshot -> {
                    Log.w("Notification Recycler data@", "status:adding");
                    Notification notification = documentSnapshot.toObject(Notification.class);
                    NotificationRecyclerViewItem item = new NotificationRecyclerViewItem();
                    assert notification != null;
                    item.message = notification.message;
                    item.date = notification.date;
                    item.isRead = notification.isRead;
                    item.notificationIcon = R.drawable.ic_coronavirus;
                    itemAdapter.add(item);
                });

            } else {
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.w("fetching data@", "status:failed", task.getException());
            }
        });
    }

    private void initRecyclerViewAdapters() {
        itemAdapter = new ItemAdapter<>();
        // init adapter
        fastAdapter = FastAdapter.with(itemAdapter);
        notificationToolBar.setAdapter(fastAdapter);
    }

    private void initComponents() {
        notificationToolBar = findViewById(R.id.notificationRecyclerView);
        notificationToolbar = findViewById(R.id.notificationToolBar);
    }

    private void initFirebase() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        whoIMetRef = firestore.collection("profiles").document(auth.getUid()).collection("notifications");
    }
}