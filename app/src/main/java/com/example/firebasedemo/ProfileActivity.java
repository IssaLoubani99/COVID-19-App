package com.example.firebasedemo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasedemo.modules.ChangeUsernameDialog;
import com.example.firebasedemo.modules.ProfileRecyclerViewItem;
import com.example.firebasedemo.modules.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

public class ProfileActivity extends AppCompatActivity {

    // Recycler view
    RecyclerView recyclerView;
    ItemAdapter<ProfileRecyclerViewItem> itemAdapter;
    FastAdapter<ProfileRecyclerViewItem> fastAdapter;
    // Current user
    User currentUser;
    // Components
    Toolbar profileToolBar;
    // Firebase
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    DocumentReference currentUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        profileToolBar = findViewById(R.id.profileToolBar);
        setSupportActionBar(profileToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initFirebase();
        initRecyclerView();
        initProfileLayouts();
        initFastAdapterListener();
    }

    private void initFirebase() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserReference = firestore.collection("profiles").document(auth.getUid());
    }

    private void initProfileLayouts() {
        ProfileRecyclerViewItem username, about, phone;

        username = new ProfileRecyclerViewItem();
        username.title = "Username";
        username.description = "This is your username. This will be visible to all your tracker contact.";
        username.value = currentUser.username;
        username.infoIcon = R.drawable.ic_user;
        username.actionIcon = R.drawable.ic_edit;


        about = new ProfileRecyclerViewItem();
        about.title = "About";
        about.description = "";
        about.value = "Hey There! I am protecting myself from covid-19 😁.";
        about.infoIcon = R.drawable.ic_about;
        about.actionIcon = R.drawable.ic_edit;

        phone = new ProfileRecyclerViewItem();
        phone.title = "Phone";
        phone.description = "";
        phone.value = "+961 " + currentUser.phone;
        phone.infoIcon = R.drawable.ic_phone_profile;
        phone.actionIcon = R.drawable.ic_edit;

        itemAdapter.add(username);
        itemAdapter.add(about);
        itemAdapter.add(phone);
    }

    private void initFastAdapterListener() {
        fastAdapter.setOnClickListener((view, adapter, item, position) -> {

            switch (position) {
                case 0: // username layout clicked
                 /* Toast.makeText(this, "Username ヾ(•ω•`)o", Toast.LENGTH_SHORT).show();
                 TODO Add an area to change username
                 add dialog */
                    ChangeUsernameDialog changeUsernameDialog = new ChangeUsernameDialog(this, currentUser.username);
                    changeUsernameDialog
                            .setPositiveButton("Save", (dialogInterface, i) -> {
                                Log.w("alert dialog@", "status:cancel Positive clicked");
                                String username = changeUsernameDialog.getInput();
                                if (!username.equals(changeUsernameDialog.getDefault_input())) {
                                    updateUsername(username);
                                }
                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> {
                                // Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                                Log.w("alert dialog@", "status:cancel Negative clicked");
                            })
                            .show();
                    break;
                case 2: // phone layout clicked

                    break;

                default:
                    break;
            }
            return false;
        });
    }

    private void updateUsername(String username) {
        Task<Void> query = currentUserReference.update("username", username);
        query.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // TODO dismiss dialog with success message
                Toast.makeText(this, "Username Updated !", Toast.LENGTH_SHORT).show();
                currentUser.username = username;
                updateUsernameRecyclerViewItem();
            } else {
                // TODO log exception along with showing error on screen
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.w("updating student@", "status:failed", task.getException());
            }
        });
    }

    private void updateUsernameRecyclerViewItem() {
        itemAdapter.getAdapterItem(0).value = currentUser.username;
        fastAdapter.notifyAdapterDataSetChanged(); // refresh recycler view
    }

    private void initRecyclerView() {
        // init components
        recyclerView = findViewById(R.id.recyclerView);
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
}