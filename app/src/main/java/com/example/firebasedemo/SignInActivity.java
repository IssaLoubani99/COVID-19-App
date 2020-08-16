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
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity implements TextWatcher {

    // firebase
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    // firebase collections
    CollectionReference profiles;
    // components
    Button loginBtn;
    Button registerBtn;
    TextInputEditText emailEditText, passwordEditText;
    TextInputLayout passwordLayout, emailLayout;
    ConstraintLayout rootView;
    Switch choiceSwitch;
    ProgressBar progressBar;
    // flags
    boolean isPhoneLogIn = false, isLoading = false;
    // form values
    String email = null, pass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // init components
        initComponents();
        // init firebase
        initFirebase();
        // init collections
        initCollections();
        // init text listener
        initTextListener();
        // buttons handler
        loginBtn.setOnClickListener(e -> {
            getData();
            loading();
            if (isValidMail(email)) signInWithEmail();
            else if (isValidMobile(email)) {
                checkIfPhoneExist()
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) { // no record found for the phone
                                    Toast.makeText(this, "No Account With That Phone exists", Toast.LENGTH_SHORT).show();
                                } else { // phone exists
                                    // TODO : go to verification activity along with the user info
                                    task.getResult().forEach(queryDocumentSnapshot -> {
                                        signInWithPhone(queryDocumentSnapshot.toObject(User.class));
                                    });
                                }
                            } else {
                                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.w("phone-exists", "status:failed", task.getException());
                            }
                            loading();
                        });
            } else {
                Toast.makeText(this, "Invalid Credential", Toast.LENGTH_SHORT).show();
                loading();
            }
        });
        registerBtn.setOnClickListener(e -> toRegisterActivity());
        choiceSwitch.setOnCheckedChangeListener((compoundButton, state) -> {
            // state is a boolean
            if (state) { // signing in with phone
                // change flag
                isPhoneLogIn = true;
                // change text
                choiceSwitch.setText(choiceSwitch.getTextOn());
                // disable password field
                passwordEditText.setEnabled(false);
                passwordLayout.setEnabled(false);
                // change emailTextField icon and hint to phone
                emailLayout.setStartIconDrawable(R.drawable.ic_phone);
                emailLayout.setHint("Phone");
            } else { // signing in with email
                // change flag
                isPhoneLogIn = false;
                // change text
                choiceSwitch.setText(choiceSwitch.getTextOff());
                // enable password field
                passwordEditText.setEnabled(true);
                passwordLayout.setEnabled(true);
                // revert changes
                emailLayout.setStartIconDrawable(R.drawable.ic_email);
                emailLayout.setHint("Email");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            toMainActivity();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        removeFieldErrors();
        // get data from form
        getData();
        if (isPhoneLogIn) {
            // copied from github
            loginBtn.setEnabled(!(email.isEmpty()));
        } else {
            // copied from github
            loginBtn.setEnabled(!(email.isEmpty()) && !(pass.isEmpty()));
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    private void toMainActivity() {
        Intent toMainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        toMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        toMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toMainActivityIntent);
    }

    private void toRegisterActivity() {
        Intent toRegisterActivityIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        toRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        toRegisterActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toRegisterActivityIntent);
    }

    private void initTextListener() {
        emailEditText.addTextChangedListener(this);
        passwordEditText.addTextChangedListener(this);
    }

    private void firebaseExceptionHandler(Task<AuthResult> firebaseTask) {
        if (firebaseTask.getException() instanceof FirebaseAuthInvalidCredentialsException) {
            FirebaseAuthInvalidCredentialsException credentialsException = (FirebaseAuthInvalidCredentialsException) firebaseTask.getException();
            switch (credentialsException.getErrorCode()) {
                case "ERROR_INVALID_EMAIL":
                    //  Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_SHORT).show();
                    emailLayout.setError("Invalid Email Format");
                    break;
                case "ERROR_WRONG_PASSWORD":
                    //  Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();
                    passwordLayout.setError("Wrong Password");
                    break;
                default:
                    Toast.makeText(this, firebaseTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }

        } else if (firebaseTask.getException() instanceof FirebaseAuthInvalidUserException) {
            Snackbar.make(rootView, "No account with that email is found", Snackbar.LENGTH_LONG).setAction("Register ?", e -> {
                toRegisterActivity();
            }).show();


        } else if (firebaseTask.getException() instanceof FirebaseTooManyRequestsException) {
            Snackbar.make(rootView, "Too many Attempts, please try again in 5 min", Snackbar.LENGTH_LONG).show();
        }
    }

    private void initComponents() {
        loginBtn = findViewById(R.id.loginBtn);
        emailEditText = findViewById(R.id.emailEditText);
        emailLayout = findViewById(R.id.emailLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordLayout = findViewById(R.id.passwordLayout);
        rootView = findViewById(R.id.signInRootView);
        progressBar = findViewById(R.id.progressBar);
        registerBtn = findViewById(R.id.registerBtn);
        choiceSwitch = findViewById(R.id.choiceSwitch);

        // TODO : phone login is disabled for now
        choiceSwitch.setEnabled(false);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void getData() {
        email = emailEditText.getText().toString().trim();
        pass = passwordEditText.getText().toString().trim();

    }

    private void removeFieldErrors() {
        if (passwordLayout.getError() != null) {
            passwordLayout.setError(null);
        }

        if (emailLayout.getError() != null) {
            emailLayout.setError(null);
        }
    }

    private void signInWithEmail() {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Auth State :", "signInWithEmail:success");
                        if (task.getResult().getUser().isEmailVerified()) {
                            toMainActivity();
                        } else {
                            Snackbar.make(rootView, "You need to verify you're Email First", Snackbar.LENGTH_LONG).show();
                            auth.signOut();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Error Auth", "signInWithEmail:failure", task.getException());
                        firebaseExceptionHandler(task);
                    }
                    loading();
                }
        );
    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void initCollections() {
        profiles = firestore.collection("profiles");
    }

    private void toVerificationActivity(User user) {
        Intent toVerificationActivityIntent = new Intent(getApplicationContext(), VerifyPhoneActivity.class);
        toVerificationActivityIntent.putExtra("user", user);
        toVerificationActivityIntent.putExtra("isSigningIn", true);
        toVerificationActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        toVerificationActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toVerificationActivityIntent);
    }

    private void signInWithPhone(User user) {
        toVerificationActivity(user);
    }

    private boolean isValidMobile(String phone) {
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            return phone.length() > 6 && phone.length() <= 13;
        }
        return false;
    }

    private void loading() {
        if (isLoading) {
            loginBtn.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            loginBtn.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        }
        // toggle loading
        isLoading = !isLoading;
    }

    private Query checkIfPhoneExist() {
        return profiles.whereEqualTo("phone", email);
    }
}