package com.paulkiren.foregroundservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import com.facebook.react.R;
import android.Manifest;
import android.content.pm.PackageManager;


// partially took ideas from: https://github.com/zo0r/react-native-push-notification/blob/master/android/src/main/java/com/dieam/reactnativepushnotification/modules/RNPushNotificationHelper.java


class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String NOTIFICATION_CHANNEL_ID = "com.paulkiren.foregroundservice.channel";

    private static NotificationHelper instance = null;
    private NotificationManager mNotificationManager;

    PendingIntent pendingBtnIntent;
    PendingIntent pendingBtn2Intent;
    private Context context;
    private NotificationConfig config;

    public static synchronized NotificationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationHelper(context);
        }
        return instance;
    }

    private NotificationHelper(Context context) {
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.context = context;
        this.config = new NotificationConfig(context);
    }

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private PendingIntent createPendingIntent(Context context, Intent intent) {
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        
        // Add FLAG_IMMUTABLE for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        return PendingIntent.getActivity(context, uniqueInt, intent, flags);
    }

    Notification buildNotification(Context context, Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "buildNotification: invalid config");
            return null;
        }
        Class mainActivityClass = getMainActivityClass(context);
        if (mainActivityClass == null) {
            return null;
        }

        Log.d(TAG,""+bundle.getString("mainOnPress"));

        Intent notificationIntent = new Intent(context, mainActivityClass);
        notificationIntent.putExtra("mainOnPress",bundle.getString("mainOnPress"));
        
        // Add flags to handle task stack properly
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = createPendingIntent(context, notificationIntent);

        if (bundle.getBoolean("button", false)) {
            Log.d(TAG, "inButtonOnPress" + bundle.getString("buttonOnPress"));
            Intent notificationBtnIntent = new Intent(context, mainActivityClass);
            notificationBtnIntent.putExtra("buttonOnPress", bundle.getString("buttonOnPress"));
            notificationBtnIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingBtnIntent = createPendingIntent(context, notificationBtnIntent);
        }

        if (bundle.getBoolean("button2", false)) {
            Log.i(TAG, "inButton2OnPress" + bundle.getString("button2OnPress"));
            Intent notificationBtn2Intent = new Intent(context, mainActivityClass);
            notificationBtn2Intent.putExtra("button2OnPress", bundle.getString("button2OnPress"));
            notificationBtn2Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingBtn2Intent = createPendingIntent(context, notificationBtn2Intent);
        }

        String title = bundle.getString("title");

        int priority = NotificationCompat.PRIORITY_HIGH;
        final String priorityString = bundle.getString("importance");

        if (priorityString != null) {
            switch(priorityString.toLowerCase()) {
                case "max":
                    priority = NotificationCompat.PRIORITY_MAX;
                    break;
                case "high":
                    priority = NotificationCompat.PRIORITY_HIGH;
                    break;
                case "low":
                    priority = NotificationCompat.PRIORITY_LOW;
                    break;
                case "min":
                    priority = NotificationCompat.PRIORITY_MIN;
                    break;
                case "default":
                    priority = NotificationCompat.PRIORITY_DEFAULT;
                    break;
                default:
                    priority = NotificationCompat.PRIORITY_HIGH;
            }
        }

        int visibility = NotificationCompat.VISIBILITY_PRIVATE;
        String visibilityString = bundle.getString("visibility");

        if (visibilityString != null) {
            switch(visibilityString.toLowerCase()) {
                case "private":
                    visibility = NotificationCompat.VISIBILITY_PRIVATE;
                    break;
                case "public":
                    visibility = NotificationCompat.VISIBILITY_PUBLIC;
                    break;
                case "secret":
                    visibility = NotificationCompat.VISIBILITY_SECRET;
                    break;
                default:
                    visibility = NotificationCompat.VISIBILITY_PRIVATE;
            }
        }

        // Ensure notification channels are set up correctly for Android O+
        NotificationChannel channel = checkOrCreateChannel(mNotificationManager, bundle);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setVisibility(visibility)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setOngoing(bundle.getBoolean("ongoing", false))
            .setContentText(bundle.getString("message"))
            .setAutoCancel(false)
            .setLocalOnly(false)
            .setDefaults(0);

        // Set foreground service type for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            notificationBuilder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE);
        }
       
        if(bundle.getBoolean("button", false) == true){
            notificationBuilder.addAction(R.drawable.redbox_top_border_background, bundle.getString("buttonText", "Button"), pendingBtnIntent);
        }

        if(bundle.getBoolean("button2", false) == true){
            notificationBuilder.addAction(R.drawable.redbox_top_border_background, bundle.getString("button2Text", "Button"), pendingBtn2Intent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(this.config.getNotificationColor());
        }
        
        String color = bundle.getString("color");
        if(color != null){
            notificationBuilder.setColor(Color.parseColor(color));
        }

        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(bundle.getString("message")));

        String iconName = bundle.getString("icon");
        
        if(iconName == null){
            iconName = "ic_launcher";
        }
        notificationBuilder.setSmallIcon(getResourceIdForResourceName(context, iconName));

        String largeIconName = bundle.getString("largeIcon");
        if(largeIconName == null){
            largeIconName = "ic_launcher";
        }

        int largeIconResId = getResourceIdForResourceName(context, largeIconName);
        Bitmap largeIconBitmap = BitmapFactory.decodeResource(context.getResources(), largeIconResId);

        if (largeIconResId != 0) {
            notificationBuilder.setLargeIcon(largeIconBitmap);
        }

        String numberString = bundle.getString("number");
        if (numberString != null) {
            int numberInt = Integer.parseInt(numberString);
            if(numberInt > 0){
                notificationBuilder.setNumber(numberInt);
            }
        }

        Boolean progress = bundle.getBoolean("progressBar");
        if(progress){
            double max = bundle.getDouble("progressBarMax");
            double curr = bundle.getDouble("progressBarCurr");
            notificationBuilder.setProgress((int)max, (int)curr,false);
        }

        notificationBuilder.setOnlyAlertOnce(true);

        return notificationBuilder.build();
    }

    private Class getMainActivityClass(Context context) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null || launchIntent.getComponent() == null) {
            Log.e(TAG, "Failed to get launch intent or component");
            return null;
        }
        try {
            return Class.forName(launchIntent.getComponent().getClassName());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed to get main activity class", e);
            return null;
        }
    }

    private int getResourceIdForResourceName(Context context, String resourceName) {
        int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        if (resourceId == 0) {
            resourceId = context.getResources().getIdentifier(resourceName, "mipmap", context.getPackageName());
        }
        return resourceId;
    }

    private static boolean channelCreated = false;
    private NotificationChannel checkOrCreateChannel(NotificationManager manager, Bundle bundle) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return null;
            
        if (manager == null)
            return null;

        String channelId = NOTIFICATION_CHANNEL_ID;
        String channelName = this.config.getChannelName();
        
        // Allow custom channel ID and name
        if (bundle.containsKey("channelId")) {
            channelId = bundle.getString("channelId");
        }
        
        if (bundle.containsKey("channelName")) {
            channelName = bundle.getString("channelName");
        }
        
        // Check if channel exists
        NotificationChannel existingChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            existingChannel = manager.getNotificationChannel(channelId);
            if (existingChannel != null) {
                return existingChannel;
            }
        }

        int importance = NotificationManager.IMPORTANCE_HIGH;
        final String importanceString = bundle.getString("importance");

        if (importanceString != null) {
            switch(importanceString.toLowerCase()) {
                case "default":
                    importance = NotificationManager.IMPORTANCE_DEFAULT;
                    break;
                case "max":
                    importance = NotificationManager.IMPORTANCE_MAX;
                    break;
                case "high":
                    importance = NotificationManager.IMPORTANCE_HIGH;
                    break;
                case "low":
                    importance = NotificationManager.IMPORTANCE_LOW;
                    break;
                case "min":
                    importance = NotificationManager.IMPORTANCE_MIN;
                    break;
                case "none":
                    importance = NotificationManager.IMPORTANCE_NONE;
                    break;
                case "unspecified":
                    importance = NotificationManager.IMPORTANCE_UNSPECIFIED;
                    break;
                default:
                    importance = NotificationManager.IMPORTANCE_HIGH;
            }
        }
        
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(this.config.getChannelDescription());
        channel.enableLights(true);
        channel.enableVibration(bundle.getBoolean("vibration", true));
        channel.setShowBadge(true);

        manager.createNotificationChannel(channel);
        return channel;
    }
}
