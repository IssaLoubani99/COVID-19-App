package com.example.firebasedemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.firebasedemo.modules.Device;
import com.example.firebasedemo.modules.NotificationMessagingService;
import com.example.firebasedemo.modules.Person;
import com.example.firebasedemo.modules.PreBuiltSuperToast;
import com.example.firebasedemo.modules.SharedPrefManager;
import com.example.firebasedemo.modules.User;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.tapadoo.alerter.Alerter;

import org.pixsee.fcm.Message;
import org.pixsee.fcm.Notification;
import org.pixsee.fcm.Sender;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import c.tlgbltcn.library.BluetoothHelper;
import c.tlgbltcn.library.BluetoothHelperListener;
import id.ionbit.ionalert.IonAlert;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, LocationListener, BluetoothHelperListener {

    // for location permission
    final int REQUEST_LOCATION_CODE = 1010;
    // permissions
    final String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    // firebase
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    // firebase messaging cloud
    Sender fcmSender;
    // firebase database references
    CollectionReference whoIMetRef, devicesRef, messagingTokenRef, profilesRef;
    DocumentReference currentUserRef;
    // components
    MapView mainMapView;
    Button mainSubmitBtn, mainScanBtn, mainInfectedBtn;
    LocationManager locationManager;
    GoogleMap mainGoogleMap;
    TextView welcomeTextView;
    Toolbar mainToolbar;
    TextInputEditText mainNameEditText, mainPhoneEditText;
    CardView mainFormCardView;
    CheckBox unknownPersonCheckBox;
    // form data
    String name, phone;
    // bluetooth helper library
    BluetoothHelper bluetooth;
    // message receiver
    BroadcastReceiver messageReceiver;
    // super toast library
    SuperToast progressBarToast, materialToast;
    // hold the current user information
    User currentUser;
    // location attributes
    double latitude = 0.0, longitude = 0.0;
    // FLAGS do not change
    boolean isLocationServiceReady = false, isBluetoothRegistered = false, deviceDiscovered = false;
    // notification counter
    public static int notificationCounter = 1;
    // menu
    Menu mainMenu;
    MenuItem notificationMenuItem;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init components
        initComponents();
        // init map
        mainMapView.onCreate(savedInstanceState);
        // init firebase
        initFirebase();
        // init firebase ref
        initFirebaseRef();
        // init user info
        initUserInfo();
        // init super toast
        initSuperToast();
        // get messaging token
        getMessagingToken();
        // init person text listeners
        initPersonTextWatcher();
        // register broadcast
        registerBroadCastReceiver();
        // init toolbar
        setSupportActionBar(mainToolbar);
        // check and get permissions
        checkAppPermission();
        // buttons handler
        mainSubmitBtn.setOnClickListener(view -> {
            mainSubmitBtn.setEnabled(false);
            addNewPersonToDatabase(true);
        });
        mainScanBtn.setOnClickListener(view -> {
            bluetoothScan();
        });
        mainInfectedBtn.setOnClickListener(view -> {

            new IonAlert(this, IonAlert.SUCCESS_TYPE)
                    .setTitleText("Stay Safe")
                    .setContentText("You will be recognized as a COVID-19 infected person, please stay home " +
                            "and contact the nearest hospital in you're area. ")
                    .setConfirmText("Yes, I will")
                    .setConfirmClickListener(sDialog -> {
                        // dismiss
                        broadcastMessage();
                        mainInfectedBtn.setVisibility(View.INVISIBLE);
                        updateUserInfo("isInfected", true)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.w("infection-update@", "status:updated");
                                    } else {
                                        Log.i("infection-update@", "status:updated", task.getException());
                                        mainInfectedBtn.setVisibility(View.VISIBLE);
                                    }
                                });

                        sDialog.dismissWithAnimation();
                    })
                    .show();
        });
        unknownPersonCheckBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                addNewPersonToDatabase(false);
            }
        });
    }

    private void addFormAnimation() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        mainFormCardView.setAnimation(animation);
    }

    private Task<Void> updateUserInfo(String field, Object value) {
        return currentUserRef.update(field, value);
    }

    /* main menu init */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_toolbar, menu);
        mainMenu = menu;
        notificationMenuItem = menu.findItem(R.id.notificationMenuBtn);
        initNotificationMenuItem(notificationMenuItem);
        return super.onCreateOptionsMenu(menu);
    }

    /* main menu button init */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logoutMenuBtn:
                auth.signOut();
                toSignInActivity();
                return true;
            case R.id.profileMenuBtn:
                Intent intentToProfile = new Intent(getApplicationContext(), ProfileActivity.class);
                intentToProfile.putExtra("currentUser", currentUser);
                startActivity(intentToProfile);
                return true;
            case R.id.whoIMetMenuBtn:
                startActivity(new Intent(getApplicationContext(), WhoIMetActivity.class));
                return true;
            case R.id.notificationMenuBtn:
                // startActivity(new Intent(getApplicationContext(), WhoIMetActivity.class));
                startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
                return true;
            default:
                return false;
        }

    }

    /* permission result */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /* permission result*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (EasyPermissions.hasPermissions(this, perms)) {
                initMapAndLocationAndBluetooth();
            } else {
                getPermissions();
            }
        }
    }

    /* life cycle */
    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        Log.i("MainMenuActivity@", "status:resumed");
        mainMapView.onResume();
        if (bluetooth != null) {
            bluetooth.registerBluetoothStateChanged();
            isBluetoothRegistered = true;
        }
        super.onResume();
        // add animation to form
        addFormAnimation();
    }

    @Override
    protected void onPause() {
        Log.i("MainMenuActivity@", "status:paused");
        if (isBluetoothRegistered) {
            bluetooth.unregisterBluetoothStateChanged();
            isBluetoothRegistered = false;
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mainMapView.onLowMemory();
    }

    /* permission listener */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_LOCATION_CODE) {
            initMapAndLocationAndBluetooth();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.i("PermissionStatus@", "status:somepermissiondenied");
        if (!EasyPermissions.hasPermissions(this, this.perms) && EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }

    }

    /* location listener */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.i("Current-Location@", "La , Lo : " + latitude + " " + longitude);
        locationManager.removeUpdates(this);
        focusOnLocation(location.getLatitude(), location.getLongitude());
    }

    /* bluetooth listeners */
    @Override
    public void getBluetoothDeviceList(BluetoothDevice bluetoothDevice) {
        String result = "Device Discovered : " + bluetoothDevice.getName();
        materialToast.setText(result);
        materialToast.show();
        // update flag
        deviceDiscovered = true;
        // upload device to database
        uploadDevice(bluetoothDevice.getName(), bluetoothDevice.getAddress());
    }

    @Override
    public void onDisabledBluetooh() {

    }

    @Override
    public void onEnabledBluetooth() {
        Toast.makeText(getApplicationContext(), "Bluetooth Is Now On", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartDiscovery() {
        Log.i("BluetoothScanning@", "status:started");
        mainScanBtn.setEnabled(false);
        progressBarToast.show();
    }

    @Override
    public void onFinishDiscovery() {
        Log.i("BluetoothScanning@", "status:finished");
        // if no device was found
        if (!deviceDiscovered) {
            materialToast.setText("No Device Found");
            materialToast.show();
        }
        // set it to false again for other scans
        deviceDiscovered = false;
        mainScanBtn.setEnabled(true);
        progressBarToast.dismiss();
    }

    // in case the gps is enabled
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.i("ProviderStatus@", "status:enabled");
    }

    // in case the gps is disabled
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.i("ProviderStatus@", "status:disabled");
        new IonAlert(this, IonAlert.WARNING_TYPE)
                .setTitleText("Location need it to be On !")
                .setContentText("This app may not work properly without GPS.")
                .setConfirmText("Yes, I 'll turn it on")
                .setConfirmClickListener(ionAlert -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    ionAlert.dismissWithAnimation();
                })
                .show();
    }

    // non Override written functions
    @SuppressLint("MissingPermission")
    private void initMapAndLocationAndBluetooth() {
        Log.w("initMapAndLocationAndBluetooth", "status:initializing");
        // init location manager
        initLocationManager();
        Log.w("google-map", "status:initializing");
        mainMapView.getMapAsync(googleMap -> {
            mainGoogleMap = googleMap;
            mainGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            mainGoogleMap.setMyLocationEnabled(true);
            Log.w("google-map", "status:success");
            //MainActivity.this.onLocationChanged(mainGoogleMap.getMyLocation());
        });
        isLocationServiceReady = true;
        Log.w("initMapAndLocationAndBluetooth", "status:success");
        initBluetooth();
    }

    @SuppressLint("MissingPermission")
    private void initLocationManager() {
        Log.w("location-manager", "status:initializing");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix
            //  otherwise wait for the update below
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            focusOnLocation(location.getLatitude(), location.getLongitude());
            Log.w("location-manager", "status:focus-on-location");
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            Log.w("location-manager", "status:requesting-update");
        }

    }

    private String getDateAndTime() {
        Date c = Calendar.getInstance().getTime();
        return c.toString();
    }

    private void toSignInActivity() {
        Intent toMainActivityIntent = new Intent(getApplicationContext(), SignInActivity.class);
        toMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        toMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toMainActivityIntent);
    }

    private void forceExit() {
        Toast.makeText(this, "Sorry, You cannot access you're account right now", Toast.LENGTH_SHORT).show();
        auth.signOut();
        toSignInActivity();
    }

    private void initComponents() {
        // init components
        mainToolbar = findViewById(R.id.toolbar);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        // google maps
        mainMapView = findViewById(R.id.mainMapView);
        // for adding new person you met
        mainNameEditText = findViewById(R.id.mainNameEditText);
        mainPhoneEditText = findViewById(R.id.mainPhoneEditText);
        // buttons
        mainScanBtn = findViewById(R.id.mainScanBtn);
        mainSubmitBtn = findViewById(R.id.mainSubmitBtn);
        mainInfectedBtn = findViewById(R.id.mainInfectedBtn);
        // form
        mainFormCardView = findViewById(R.id.mainFormCardView);
        unknownPersonCheckBox = findViewById(R.id.unknownPersonCheckBox);
    }

    private void initFirebase() {
        // firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void initUserInfo() {
        // firebase profiles
        currentUserRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser = task.getResult().toObject(User.class);
                showWelcomeText();

                if (currentUser.isInfected) {
                    mainInfectedBtn.setVisibility(View.INVISIBLE);
                } else {
                    mainInfectedBtn.setVisibility(View.VISIBLE);
                }

            } else {
                forceExit();
            }
        });
    }

    private void showWelcomeText() {
        String msg = "Welcome " + currentUser.username;
        welcomeTextView.setText(msg);
        welcomeTextView.setVisibility(View.VISIBLE);
    }

    private void getPermissions() {
        if (!EasyPermissions.hasPermissions(getApplicationContext(), perms))
            EasyPermissions.requestPermissions(this, "Some Permission are needed for the app to work properly",
                    REQUEST_LOCATION_CODE, perms);
    }

    private void getData() {
        name = mainNameEditText.getText().toString().trim();
        phone = mainPhoneEditText.getText().toString().trim();
    }

    private void initBluetooth() {
        try {
            bluetooth = new BluetoothHelper(this, this)
                    .create();
        } catch (IllegalStateException exception) {
            Toast.makeText(this, "Phone does not support Bluetooth", Toast.LENGTH_SHORT).show();
            mainScanBtn.setEnabled(false);
        }
    }

    private void turnOnBluetooth() {
        if (!bluetooth.isBluetoothEnabled()) {
            bluetooth.enableBluetooth();
        }
    }

    private void bluetoothScan() {
        turnOnBluetooth();
        bluetooth.startDiscovery();

    }

    private void initSuperToast() {
        progressBarToast = PreBuiltSuperToast.progressBarToast(this, "Scanning");
        materialToast = PreBuiltSuperToast.snackBarToast(this, "");
    }

    private void initFirebaseRef() {
        profilesRef = firestore.collection("profiles");
        currentUserRef = profilesRef.document(auth.getUid());
        whoIMetRef = currentUserRef.collection("whoimet");
        devicesRef = currentUserRef.collection("devices");
        messagingTokenRef = currentUserRef.collection("messages-tokens");
    }

    private Person getNewPerson(boolean isKnown) {
        if (isKnown) {
            return new Person(name, phone, getDateAndTime(), latitude, longitude);
        } else {
            return new Person("n/a", "n/a", getDateAndTime(), latitude, longitude);
        }
    }

    private void addNewPersonToDatabase(boolean isKnown) {
        whoIMetRef.add(getNewPerson(isKnown))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Person met was Saved !", Toast.LENGTH_SHORT).show();
                        clearPersonForm();
                    } else {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        mainSubmitBtn.setEnabled(true);
                    }

                });
    }

    private void initPersonTextWatcher() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getData();
                mainSubmitBtn.setEnabled(!(name.isEmpty()) && !(phone.isEmpty()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        mainNameEditText.addTextChangedListener(textWatcher);
        mainPhoneEditText.addTextChangedListener(textWatcher);

    }

    private void checkAppPermission() {

        if (EasyPermissions.hasPermissions(this, perms)) {
            Log.w("check-permission", "status:available");
            initMapAndLocationAndBluetooth();
        } else {
            Log.w("check-permission", "status:unavailable");
            getPermissions();
        }
    }

    private void clearPersonForm() {
        name = phone = ""; // set values to empty
        // name
        mainNameEditText.setText(""); // clear text
        mainNameEditText.clearFocus(); // remove focus
        // phone
        mainPhoneEditText.setText(""); // clear text
        mainPhoneEditText.clearFocus(); // remove focus
    }

    private void uploadDevice(String name, String mac) {
        Device device = new Device(name, mac, latitude, longitude);
        devicesRef.add(device)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendTokenToServer(String token) {
        // hold the value of the token
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        messagingTokenRef.document(token).set(map).addOnCompleteListener(tokenUploadTask -> {
            if (tokenUploadTask.isSuccessful()) {
                Log.i("token-upload@", "status:success");
            } else {
                Log.w("token-upload@", "status:failed", tokenUploadTask.getException());
            }
        });
    }

    private void saveToken(String token) {
        SharedPrefManager.getInstance(MainActivity.this).storeToken(token);
    }

    private void getMessagingToken() {
        if (SharedPrefManager.getInstance(MainActivity.this).getToken() == null) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener
                    (instanceIdResult -> {
                        Log.i("MessagingToken@", instanceIdResult.getToken());
                        saveToken(instanceIdResult.getToken());
                        sendTokenToServer(instanceIdResult.getToken());
                    });
        }
    }

    private void registerSender() {
        fcmSender = new Sender(getString(R.string.server_key));
    }

    private Message buildMessage(String token, String title, String message) {
        Notification notification = new Notification(title, message);
        return new Message.MessageBuilder()
                .toToken(token) // single android/ios device
                .notification(notification)
                .addData("message", message)
                .addData("title", title)
                .priority(Message.Priority.HIGH)
                .build();
    }

    private void sendMessage(Message message) {
        registerSender();
        fcmSender.send(message);
    }

    private void broadcastMessage() {
        firestore.collectionGroup("messages-tokens")
                .get()
                .addOnFailureListener(e ->
                        Log.w("FetchingToken@", "status:failed", e)
                )
                .addOnSuccessListener(documentSnapshots -> {
                    documentSnapshots.forEach(queryDocumentSnapshot -> {
                        Log.i("token@", "status:send");
                        Log.i("notification@", "status:send");
                        //queryDocumentSnapshot.getId(), currentUser.username + " was registered as a COVID-19 patient" +
                        //". Please contact you're nearest hospital if you contact " + currentUser.username + " ."
                        String deviceToken = queryDocumentSnapshot.getId();
                        if (!deviceToken.equals(SharedPrefManager.getInstance(MainActivity.this).getToken())) {
                            sendMessage(buildMessage(deviceToken, "COVID-19 Alert", currentUser.username + " was registered as a COVID-19 patient" +
                                    ". Please contact you're nearest hospital if you contact " + currentUser.username + " ."));
                        }
                    });
                });
    }

    private void initMessageReceiver() {
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onMessageReceived(context, intent);
            }
        };
    }

    private void registerBroadCastReceiver() {
        initMessageReceiver();
        registerReceiver(messageReceiver, new IntentFilter(NotificationMessagingService.MESSAGE_BROADCAST));
    }

    private void onMessageReceived(Context context, Intent intent) {
        // get message
        String msg = intent.getStringExtra("message");
        // get title
        String title = intent.getStringExtra("title");
        // create alert with title and message
        Alerter.create(this)
                .setTitle(title)
                .setText(msg)
                .enableSwipeToDismiss()
                .setTitleAppearance(R.style.TitleStyle)
                .setTextAppearance(R.style.MessageStyle)
                .setIcon(R.drawable.ic_covid)
                .setIconColorFilter(0) // Optional - Removes white tint
                .setBackgroundColorRes(R.color.infectedColor) // or setBackgroundColorInt(Color.CYAN)
                .show();
      /* Note:
         long name i know o((>ω< ))o
         that occurs because there is another class in this activity named notification
         so to distinguish between them i needed to call it from it's package and not by import */
        com.example.firebasedemo.modules.Notification notification = new com.example.firebasedemo.modules.Notification();

        notification.date = System.currentTimeMillis();
        notification.isRead = false;
        notification.message = msg;
        // update notification counter
        ActionItemBadge.update(notificationMenuItem, ++notificationCounter);
        currentUserRef.collection("notifications").document().set(notification)
                .addOnCompleteListener(task -> {
                    if (task.isComplete()) {
                        Log.w("Message-Notification-Upload@", "status:success");
                    } else {
                        Log.w("Message-Notification-Upload@", "status:error", task.getException());
                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void focusOnLocation(double la, double lo) {
        if (isLocationServiceReady && mainGoogleMap != null) {
            mainGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(la, lo), 15));
        }
    }

    private void initNotificationMenuItem(MenuItem notification) {


        ActionItemBadge.update(
                this,
                notification,
                ContextCompat.getDrawable(this, R.drawable.ic_notification),
                ActionItemBadge.BadgeStyles.RED,
                notificationCounter
        );

    }
}