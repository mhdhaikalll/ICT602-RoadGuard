package com.example.roadguard.service;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.roadguard.MainActivity;
import com.example.roadguard.R;
import com.example.roadguard.data.remote.FirestoreDataSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;
import java.util.Map;

public class RoadGuardFCMService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "roadguard_alerts";
    private TextToSpeech tts;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        // Initialize TTS engine
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
            }
        });
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Update token in Firestore for the logged-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            new FirestoreDataSource().updateFCMToken(user.getUid(), token)
                    .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated"))
                    .addOnFailureListener(e -> Log.e("FCM", "Token update failed", e));
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "Road Hazard Alert";
        String body = "A new hazard was reported near you.";
        Map<String, String> data = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }
        if (remoteMessage.getData() != null) {
            data = remoteMessage.getData();
        }

        // Show notification
        showNotification(title, body, data);

        // Speak the notification body aloud (if user has TTS enabled)
        speakAlert(body);
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Pass coordinates to center the map
        if (data != null) {
            if (data.containsKey("latitude")) {
                intent.putExtra("notification_lat", Double.parseDouble(data.get("latitude")));
            }
            if (data.containsKey("longitude")) {
                intent.putExtra("notification_lng", Double.parseDouble(data.get("longitude")));
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hazard_marker)  // reuse the marker icon
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, builder.build());
        }
    }

    private void speakAlert(String text) {
        if (text == null || text.isEmpty()) return;

        // Optionally check the TTS preference from Room or SharedPreferences
        // For now, we speak unconditionally. You can refine later.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "roadguard_alert");
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Hazard Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for nearby road hazards");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
