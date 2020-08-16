package com.example.firebasedemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.firebasedemo.modules.User;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class RegisterActivity extends AppCompatActivity implements TextWatcher {

    // components
    Button toSignInActivityBtn, rRegisterBtn;
    Switch regSwitch;
    ProgressBar rProgressBar;
    TextInputEditText rUsernameEditText, rEmailEditText, rPasswordEditText, rPhoneEditText;
    TextInputLayout rUsernameLayout, rEmailLayout, rPasswordLayout, rPhoneLayout;
    ConstraintLayout registerRootView;
    // firestore
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    PhoneAuthProvider phoneAuthProvider;
    // form values
    String email = null, password = null, username = null, phone = null;
    // firebase collection
    CollectionReference profiles;
    // flags
    boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // initialise components
        initComponents();
        // initialise firebase
        initFirebase();
        // firebase references
        initFirebaseRef();
        // initialise text listeners
        initTextListener();
        // event handler
        toSignInActivityBtn.setOnClickListener(e -> toSignInActivity());
        rRegisterBtn.setOnClickListener(e -> {
            getData();
            // Toast.makeText(this, username + " " + email + " " + password + " " + phone, Toast.LENGTH_SHORT).show();
            if (username.length() < 3) {
                rUsernameLayout.setError("Username must be 3 character or more");
                allowResubmit();
                return;
            } else if (phone.length() < 8) {
                rPhoneLayout.setError("Phone number must be 8 character or more");
                allowResubmit();
                return;
            }
            loading();
            if (regSwitch.isChecked()) { // phone verification
                checkIfPhoneExist()
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) { // no record found for the phone
                                    registerWithPhone();
                                } else { // phone exists
                                    // TODO : go to verification activity along with the user info
                                    Toast.makeText(this, "An Account With That Phone exists", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.w("phone-exists", "status:failed", task.getException());
                            }
                            loading();
                        });

            } else { // email verification
                verifyWithEmail();
            }

        });
        regSwitch.setOnCheckedChangeListener((compoundButton, state) -> {
            // state is a boolean
            if (state) {
                // change text
                regSwitch.setText(regSwitch.getTextOn());
                rEmailEditText.setText("N/A");
                rPasswordEditText.setText("N/A");
                rEmailLayout.setVisibility(View.GONE);
                rPasswordLayout.setVisibility(View.GONE);
            } else {
                // change text
                rEmailEditText.setText("");
                rPasswordEditText.setText("");
                regSwitch.setText(regSwitch.getTextOff());
                rEmailLayout.setVisibility(View.VISIBLE);
                rPasswordLayout.setVisibility(View.VISIBLE);

            }
        });
    }

    private Query checkIfPhoneExist() {
        return profiles.whereEqualTo("phone", phone);
    }

    private void initFirebaseRef() {
        profiles = firestore.collection("profiles");
    }

    private void verifyWithEmail() {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Auth-State@", "createAccount:success");
                        authTask.getResult().getUser().sendEmailVerification()
                                .addOnSuccessListener(__ -> {
                                    saveUser(authTask.getResult().getUser().getUid());
                                });
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Error Auth", "createAccount:failure", authTask.getException());
                        firebaseExceptionHandler(authTask);
                    }

                    auth.signOut();
                    loading();
                });
    }

    private void registerWithPhone() {
        User currentUser = getUserInfo();
        Log.i("CurrentUserInfo@", currentUser.toString());
        toVerificationActivity(currentUser);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // remove field errors
        removeFieldErrors();
        // ger data from form
        getData();
        // copied from github
        rRegisterBtn.setEnabled(!(email.isEmpty()) && !(password.isEmpty()) && !(username.isEmpty()) && !(phone.isEmpty()));
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private void firebaseExceptionHandler(Task<AuthResult> firebaseTask) {
        if (firebaseTask.getException() instanceof FirebaseAuthInvalidCredentialsException) {
            FirebaseAuthInvalidCredentialsException credentialsException = (FirebaseAuthInvalidCredentialsException) firebaseTask.getException();

            switch (credentialsException.getErrorCode()) {
                case "ERROR_INVALID_EMAIL":
                    //  Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_SHORT).show();
                    rEmailLayout.setError("Invalid Email Format");
                    break;
                case "ERROR_WEAK_PASSWORD":
                    rPasswordLayout.setError("The password is invalid it must 6 characters at least");
                    break;

                default:
                    Toast.makeText(this, firebaseTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }


        } else if (firebaseTask.getException() instanceof FirebaseAuthUserCollisionException) {
            rEmailLayout.setError("The email address is already in use by another account.");
        } else {
            Toast.makeText(this, firebaseTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initComponents() {
        registerRootView = findViewById(R.id.registerRootView);
        toSignInActivityBtn = findViewById(R.id.toSignInActivityBtn);
        rRegisterBtn = findViewById(R.id.rRegisterBtn);

        rUsernameEditText = findViewById(R.id.rUsernameEditText);
        rEmailEditText = findViewById(R.id.rEmailEditText);
        rPasswordEditText = findViewById(R.id.rPasswordEditText);
        rPhoneEditText = findViewById(R.id.rPhoneEditText);


        rUsernameLayout = findViewById(R.id.rUsernameLayout);
        rEmailLayout = findViewById(R.id.rEmailLayout);
        rPasswordLayout = findViewById(R.id.rPasswordLayout);
        rPhoneLayout = findViewById(R.id.rPhoneLayout);

        rProgressBar = findViewById(R.id.rProgressBar);
        regSwitch = findViewById(R.id.regSwitch);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        phoneAuthProvider = PhoneAuthProvider.getInstance();
    }

    private void initTextListener() {
        rUsernameEditText.addTextChangedListener(this);
        rEmailEditText.addTextChangedListener(this);
        rPasswordEditText.addTextChangedListener(this);
        rPhoneEditText.addTextChangedListener(this);
    }

    private void toSignInActivity() {
        Intent toSignInActivityIntent = new Intent(getApplicationContext(), SignInActivity.class);
        toSignInActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        toSignInActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toSignInActivityIntent);
    }

    private void toVerificationActivity(User user) {
        Intent toVerificationActivityIntent = new Intent(getApplicationContext(), VerifyPhoneActivity.class);
        toVerificationActivityIntent.putExtra("user", user);
        toVerificationActivityIntent.putExtra("isSigningIn", false);
        toVerificationActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        toVerificationActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toVerificationActivityIntent);
    }

    private void getData() {
        // .trim() remove unnecessary spaces
        username = rUsernameEditText.getText().toString().trim();
        email = rEmailEditText.getText().toString().trim();
        password = rPasswordEditText.getText().toString().trim();
        phone = rPhoneEditText.getText().toString().trim();
    }

    private void allowResubmit() {
        rRegisterBtn.setEnabled(true);
        rProgressBar.setVisibility(View.INVISIBLE);
    }

    private void removeFieldErrors() {
        // from github
        if (rPasswordLayout.getError() != null) {
            rPasswordLayout.setError(null);
        }

        if (rUsernameLayout.getError() != null) {
            rUsernameLayout.setError(null);
        }

        if (rEmailLayout.getError() != null) {
            rEmailLayout.setError(null);
        }

        if (rPhoneLayout.getError() != null) {
            rPhoneLayout.setError(null);
        }

    }

    private User getUserInfo() {
        return new User(username, email, password, phone, false);
    }

    private void saveUser(String uid) {
        profiles.document(uid).set(getUserInfo())
                .addOnSuccessListener(___ -> {

                    Log.w("WritingStatus@", "writingToDatabase:success");
                    Snackbar.make(registerRootView, "A Verification mail has been sent", Snackbar.LENGTH_LONG)
                            .setAction("Login ?", view -> {
                                toSignInActivity();
                            }).show();

                })
                .addOnFailureListener(e1 -> {
                    Log.w("WritingStatus@", "writingToDatabase:failure", e1);
                });

    }

    private void loading() {
        if (isLoading) {
            rRegisterBtn.setEnabled(true);
            rProgressBar.setVisibility(View.INVISIBLE);
        } else {
            rRegisterBtn.setEnabled(false);
            rProgressBar.setVisibility(View.VISIBLE);
        }
        // toggle loading
        isLoading = !isLoading;
    }
}