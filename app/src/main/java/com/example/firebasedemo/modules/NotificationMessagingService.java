package com.example.firebasedemo.modules;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationMessagingService extends FirebaseMessagingService {
    public static final String MESSAGE_BROADCAST = "MESSAGE_BROADCAST";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i("Message-Service@", remoteMessage.getData().toString());

        Intent broadcastIntent = new Intent(MESSAGE_BROADCAST);
        broadcastIntent.putExtra("title", remoteMessage.getData().get("title"));
        broadcastIntent.putExtra("message", remoteMessage.getData().get("message"));
        getApplicationContext().sendBroadcast(broadcastIntent);

    }

}
