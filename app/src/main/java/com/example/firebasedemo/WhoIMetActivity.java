package com.example.firebasedemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasedemo.modules.Person;
import com.example.firebasedemo.modules.WhoIMetRecyclerViewItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

public class WhoIMetActivity extends AppCompatActivity {
    // firebase init
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    // ref
    CollectionReference whoIMetRef;
    Toolbar whoIMetToolbar;
    // components
    RecyclerView whoIMetRecyclerView;
    ItemAdapter<WhoIMetRecyclerViewItem> itemAdapter;
    FastAdapter<WhoIMetRecyclerViewItem> fastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_i_met);

        initFirebase();
        initComponents();
        initRecyclerViewAdapters();
        whoIMetToolbar = findViewById(R.id.whoIMetToolbar);
        setSupportActionBar(whoIMetToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fastAdapter.setOnClickListener((view, adapter, item, position) -> {
            //Toast.makeText(this, "Clicked @ " + position, Toast.LENGTH_SHORT).show();
            Intent toPersonActivity = new Intent(getApplicationContext(), PersonActivity.class);
            toPersonActivity.putExtra("dataHolder", item.dataHolder);
            startActivity(toPersonActivity);
            return true;
        });

        fetchData();
    }

    private void fetchData() {
        whoIMetRef.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                Log.w("fetching data@", "status:success");
                task.getResult().getDocuments().forEach(documentSnapshot -> {
                    Log.w("WhoIMet Recycler data@", "status:adding");
                    Person person = documentSnapshot.toObject(Person.class);
                    WhoIMetRecyclerViewItem item = new WhoIMetRecyclerViewItem();
                    item.name = person.name;
                    item.phone = person.phone;
                    item.date = person.date;
                    item.profilePic = R.drawable.ic_user;
                    item.dataHolder = new WhoIMetRecyclerViewItem.DataHolder(person.name, person.phone, person.date, documentSnapshot.getId());
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
        whoIMetRecyclerView.setAdapter(fastAdapter);
    }

    private void initComponents() {
        whoIMetRecyclerView = findViewById(R.id.whoIMetRecyclerView);
    }

    private void initFirebase() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        whoIMetRef = firestore.collection("profiles").document(auth.getUid()).collection("whoimet");
    }
}