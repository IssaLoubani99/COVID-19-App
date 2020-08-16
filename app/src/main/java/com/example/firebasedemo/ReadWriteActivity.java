package com.example.firebasedemo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ReadWriteActivity extends AppCompatActivity {

    final private String collection = "Dummy";
    final private String id = "valueToWrite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_write);

        // FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        myRef.child("DD").setValue("Hello");

        EditText writeEditText = findViewById(R.id.writeEditText);
        EditText readEditText = findViewById(R.id.readEditText);

        Button readBtn = findViewById(R.id.readBtn);
        Button writeBtn = findViewById(R.id.writeBtn);

        writeBtn.setOnClickListener(e -> {
            String valueToWrite = writeEditText.getText().toString();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("value", valueToWrite);

//            db.collection(collection).document(id).set(hashMap).addOnSuccessListener(runnable -> {
//
//                Toast.makeText(this, "Data Written Successfully", Toast.LENGTH_SHORT).show();
//            });
        });

        readBtn.setOnClickListener(e -> {

//            db.collection(collection).document(id).addSnapshotListener((value, error) -> {
//
//                readEditText.setText(value.get("value").toString());
//
//            });

        });
    }
}