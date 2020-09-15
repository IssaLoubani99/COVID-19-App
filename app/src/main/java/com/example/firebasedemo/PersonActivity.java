package com.example.firebasedemo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasedemo.modules.ChangeFieldDialog;
import com.example.firebasedemo.modules.ProfileRecyclerViewItem;
import com.example.firebasedemo.modules.WhoIMetRecyclerViewItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

public class PersonActivity extends AppCompatActivity {
    // Recycler view
    RecyclerView recyclerView;
    ItemAdapter<ProfileRecyclerViewItem> itemAdapter;
    FastAdapter<ProfileRecyclerViewItem> fastAdapter;
    // Current user
    WhoIMetRecyclerViewItem.DataHolder currentPerson;
    // Components
    Toolbar personToolBar;
    // Firebase
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    DocumentReference personRef;

    public static final int EDIT_NAME = 0;
    public static final int EDIT_PHONE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        currentPerson = (WhoIMetRecyclerViewItem.DataHolder) getIntent().getSerializableExtra("dataHolder");
        personToolBar = findViewById(R.id.personToolBar);
        personToolBar.setTitle(currentPerson.name);
        // init back btn
        setSupportActionBar(personToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initFirebase();
        initRecyclerView();
        initPersonLayouts();
        initFastAdapterListener();

    }

    private void initFastAdapterListener() {
        fastAdapter.setOnClickListener((view, adapter, item, position) -> {

            switch (position) {
                case EDIT_NAME:
                    // TODO edit name
                    ChangeFieldDialog nameDialog = new ChangeFieldDialog(this, currentPerson.name);
                    nameDialog
                            .setPositiveButton("Save", (dialogInterface, i) -> {
                                // Toast.makeText(this, "Saving@name", Toast.LENGTH_SHORT).show();
                                if (!nameDialog.getDefault_input().equals(nameDialog.getInput())) {
                                    updateField("name", nameDialog.getInput());
                                    itemAdapter.getAdapterItem(EDIT_NAME).value = currentPerson.name;
                                    fastAdapter.notifyAdapterDataSetChanged(); // refresh recycler view
                                }

                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> {
                                //Toast.makeText(this, "Cancelling@name", Toast.LENGTH_SHORT).show();
                            }).show();

                    return true;

                case EDIT_PHONE:
                    // TODO edit phone
                    ChangeFieldDialog phoneDialog = new ChangeFieldDialog(this, currentPerson.phone);

                    phoneDialog
                            .setPositiveButton("Save", (dialogInterface, i) -> {

                                if (!phoneDialog.getDefault_input().equals(phoneDialog.getInput())) {
                                    updateField("phone", phoneDialog.getInput());
                                    itemAdapter.getAdapterItem(EDIT_PHONE).value = currentPerson.phone;
                                    fastAdapter.notifyAdapterDataSetChanged(); // refresh recycler view
                                }


                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> {
                                // Toast.makeText(this, "Cancelling@phone", Toast.LENGTH_SHORT).show();

                            }).show();

                    return true;

                default:
                    return false;
            }
        });
    }

    private void updateField(String field, String value) {
        personRef
                .update(field, value)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.w("update field@", "status:success", task.getException());
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.w("update field@", "status:failed", task.getException());
                    }

                });
    }

    private void initPersonLayouts() {
        ProfileRecyclerViewItem name, phone;

        name = new ProfileRecyclerViewItem();
        name.title = "Name";
        name.description = "This is the name of the person you met.";
        name.value = currentPerson.name;
        name.infoIcon = R.drawable.ic_user;
        name.actionIcon = R.drawable.ic_edit;


        phone = new ProfileRecyclerViewItem();
        phone.title = "Phone";
        phone.description = "";
        phone.value = currentPerson.phone;
        phone.infoIcon = R.drawable.ic_phone_profile;
        phone.actionIcon = R.drawable.ic_edit;


        itemAdapter.add(name);
        itemAdapter.add(phone);
    }

    private void initRecyclerView() {
        // init components
        recyclerView = findViewById(R.id.personRecyclerView);
        initFastAdapter();
        // set adapter
        recyclerView.setAdapter(fastAdapter);
    }

    private void initFastAdapter() {
        // init item adapter
        itemAdapter = new ItemAdapter<>();
        // init adapter
        fastAdapter = FastAdapter.with(itemAdapter);
    }

    private void initFirebase() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        personRef = firestore.collection("profiles").document(auth.getUid()).collection("whoimet").document(currentPerson.id);
    }
}