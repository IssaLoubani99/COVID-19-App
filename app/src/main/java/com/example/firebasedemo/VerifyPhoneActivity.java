package com.example.firebasedemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.example.firebasedemo.modules.User;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class VerifyPhoneActivity extends AppCompatActivity {

    // firebase
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    CollectionReference profiles;
    // phone verification
    PhoneAuthProvider phoneAuthProvider;
    String verificationID, verificationCode;
    // components
    Button verVerifyBtn;
    PinView pinView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        // init components
        initComponents();
        // init firebase
        initFirebase();
        // get user phone
        String phone = "+961" + getUserInfo().phone;
        // send verification code
        sendVerificationCode(phone);
        // buttons handler
        verVerifyBtn.setOnClickListener(view -> {
            verVerifyBtn.setEnabled(false);
            String code = pinView.getText().toString().trim();
            verifyCode(code);
        });
    }

    // phone verification functions
    private void sendVerificationCode(String phone) {
        phoneAuthProvider.verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationID = s;
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        String code = phoneAuthCredential.getSmsCode();
                        verificationCode = code;
                        pinView.setText(code);
                        verVerifyBtn.setEnabled(false);
                        if (code != null)
                            verifyCode(code);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.w("PhoneAuthProvider@", "onVerificationFailed:failure", e);
                        Toast.makeText(VerifyPhoneActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        verVerifyBtn.setEnabled(true);
                    }
                });        // OnVerificationStateChangedCallbacks
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onVerificationSuccess(task.getResult().getUser().getUid());
                    } else {
                        Log.w("Error Auth", "signinwithcredidential:failure", task.getException());
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        verVerifyBtn.setEnabled(true);
                    }
                });
    }

    private void initFirebase() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        phoneAuthProvider = PhoneAuthProvider.getInstance();
        initProfilesRef();
    }

    private void initComponents() {
        verVerifyBtn = findViewById(R.id.verVerifyBtn);
        pinView = findViewById(R.id.pinView);
    }

    private void initProfilesRef() {
        // firebase profiles
        profiles = firestore.collection("profiles");
    }

    private void toMainActivity() {
        Intent toMainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        toMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        toMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toMainActivityIntent);
    }

    private void onVerificationSuccess(String uid) {
        if (!isSigningIn()) {
            saveUserToDatabase(uid);
        }
        toMainActivity();
        verVerifyBtn.setEnabled(false);
    }

    private void saveUserToDatabase(String uid) {
        profiles.document(uid).set(getUserInfo())
                .addOnSuccessListener(___ -> {
                    Log.w("WritingStatus@", "writingToDatabase:success");
                })
                .addOnFailureListener(e1 -> {
                    Log.w("WritingStatus@", "writingToDatabase:failure", e1);
                    saveUserToDatabase(uid);
                });

    }

    private User getUserInfo() {
        return (User) Objects.requireNonNull(getIntent().getExtras()).get("user");
    }

    private boolean isSigningIn() {
        return (boolean) getIntent().getExtras().get("isSigningIn");
    }

}