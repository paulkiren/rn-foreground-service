package com.supersami.foregroundservice;

import android.content.ComponentName;
import android.content.Intent;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;

import static com.supersami.foregroundservice.ForegroundService.NOTIFICATION_CONFIG;
import static com.supersami.foregroundservice.ForegroundService.TASK_CONFIG;
import static com.supersami.foregroundservice.ForegroundService.ACTION_FOREGROUND_SERVICE_START;
import static com.supersami.foregroundservice.ForegroundService.ACTION_UPDATE_NOTIFICATION;
import static com.supersami.foregroundservice.ForegroundService.ACTION_FOREGROUND_SERVICE_STOP;
import static com.supersami.foregroundservice.ForegroundService.ACTION_FOREGROUND_SERVICE_STOP_ALL;
import static com.supersami.foregroundservice.ForegroundService.ACTION_FOREGROUND_RUN_TASK;

@ReactModule(name = ForegroundServiceModule.NAME)
public class ForegroundServiceModule extends ReactContextBaseJavaModule {
    private static final String TAG = "ForegroundServiceModule";
    
    // Error constants
    private static final String ERROR_INVALID_CONFIG = "ERROR_INVALID_CONFIG";
    private static final String ERROR_SERVICE_ERROR = "ERROR_SERVICE_ERROR";
    private static final String ERROR_PERMISSION_DENIED = "ERROR_PERMISSION_DENIED";

    // Module name for the new architecture
    public static final String NAME = "ForegroundService";

    private final ReactApplicationContext reactContext;

    public ForegroundServiceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    private boolean isRunning(){
        // Get the ForegroundService running value
        ForegroundService instance = ForegroundService.getInstance();
        int res = 0;
        if(instance != null){
            res = instance.isRunning();
        }
        return res > 0;
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return reactContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @ReactMethod
    public void startService(ReadableMap notificationConfig, Promise promise) {
        if (notificationConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Notification config is invalid");
            return;
        }

        if (!notificationConfig.hasKey("id")) {
            promise.reject(ERROR_INVALID_CONFIG , "ForegroundService: id is required");
            return;
        }

        if (!notificationConfig.hasKey("title")) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: title is required");
            return;
        }

        if (!notificationConfig.hasKey("message")) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: message is required");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            promise.reject(ERROR_PERMISSION_DENIED, "ForegroundService: POST_NOTIFICATIONS permission required for Android 13+");
            return;
        }

        try{
            Intent intent = new Intent(getReactApplicationContext(), ForegroundService.class);
            intent.setAction(ACTION_FOREGROUND_SERVICE_START);
            intent.putExtra(NOTIFICATION_CONFIG, Arguments.toBundle(notificationConfig));
            
            // Use startForegroundService for Android O+
            ComponentName componentName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                componentName = getReactApplicationContext().startForegroundService(intent);
            } else {
                componentName = getReactApplicationContext().startService(intent);
            }

            if (componentName != null) {
                promise.resolve(null);
            } else {
                promise.reject(ERROR_SERVICE_ERROR, "ForegroundService: Foreground service failed to start.");
            }
        }
        catch(Exception e){
            promise.reject(ERROR_SERVICE_ERROR, "ForegroundService: Foreground service failed to start: " + e.getMessage());
        }
    }

    @ReactMethod
    public void updateNotification(ReadableMap notificationConfig, Promise promise) {
        if (notificationConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Notification config is invalid");
            return;
        }

        if (!notificationConfig.hasKey("id")) {
            promise.reject(ERROR_INVALID_CONFIG , "ForegroundService: id is required");
            return;
        }

        if (!notificationConfig.hasKey("title")) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: title is required");
            return;
        }

        if (!notificationConfig.hasKey("message")) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: message is required");
            return;
        }

        try {
            Intent intent = new Intent(getReactApplicationContext(), ForegroundService.class);
            intent.setAction(ACTION_UPDATE_NOTIFICATION);
            intent.putExtra(NOTIFICATION_CONFIG, Arguments.toBundle(notificationConfig));
            
            ComponentName componentName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                componentName = getReactApplicationContext().startForegroundService(intent);
            } else {
                componentName = getReactApplicationContext().startService(intent);
            }

            if (componentName != null) {
                promise.resolve(null);
            } else {
                promise.reject(ERROR_SERVICE_ERROR, "Update notification failed.");
            }
        }
        catch(Exception e){
            promise.reject(ERROR_SERVICE_ERROR, "Update notification failed: " + e.getMessage());
        }
    }

    // helper to dismiss a notification. Useful if we used multiple notifications
    // for our service since stopping the foreground service will only dismiss one notification
    @ReactMethod
    public void cancelNotification(ReadableMap notificationConfig, Promise promise) {
        if (notificationConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Notification config is invalid");
            return;
        }

        if (!notificationConfig.hasKey("id")) {
            promise.reject(ERROR_INVALID_CONFIG , "ForegroundService: id is required");
            return;
        }

        try{
            int id = (int)notificationConfig.getDouble("id");

            NotificationManager mNotificationManager = (NotificationManager)this.reactContext.getSystemService(this.reactContext.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(id);

            promise.resolve(null);
        }
        catch(Exception e){
            promise.reject(ERROR_SERVICE_ERROR, "Failed to cancel notification: " + e.getMessage());
        }
    }

    @ReactMethod
    public void stopService(Promise promise) {
        try {
            // stop main service
            Intent intent = new Intent(getReactApplicationContext(), ForegroundService.class);
            intent.setAction(ACTION_FOREGROUND_SERVICE_STOP);

            // Looks odd, but we do indeed send the stop flag with a start command
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getReactApplicationContext().startForegroundService(intent);
            } else {
                getReactApplicationContext().startService(intent);
            }

            promise.resolve(null);
        } catch(Exception e) {
            promise.reject(ERROR_SERVICE_ERROR, "Service stop failed: " + e.getMessage());
        }
    }

    @ReactMethod
    public void stopServiceAll(Promise promise) {
        try {
            // stop main service with all action
            Intent intent = new Intent(getReactApplicationContext(), ForegroundService.class);
            intent.setAction(ACTION_FOREGROUND_SERVICE_STOP_ALL);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getReactApplicationContext().startForegroundService(intent);
            } else {
                getReactApplicationContext().startService(intent);
            }

            promise.resolve(null);
        } catch(Exception e) {
            promise.reject(ERROR_SERVICE_ERROR, "Service stop all failed: " + e.getMessage());
        }
    }

    @ReactMethod
    public void runTask(ReadableMap taskConfig, Promise promise) {
        if (!taskConfig.hasKey("taskName")) {
            promise.reject(ERROR_INVALID_CONFIG, "taskName is required");
            return;
        }

        if (!taskConfig.hasKey("delay")) {
            promise.reject(ERROR_INVALID_CONFIG, "delay is required");
            return;
        }

        try {
            Intent intent = new Intent(getReactApplicationContext(), ForegroundService.class);
            intent.setAction(ACTION_FOREGROUND_RUN_TASK);
            intent.putExtra(TASK_CONFIG, Arguments.toBundle(taskConfig));

            ComponentName componentName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                componentName = getReactApplicationContext().startForegroundService(intent);
            } else {
                componentName = getReactApplicationContext().startService(intent);
            }

            if (componentName != null) {
                promise.resolve(null);
            } else {
                promise.reject(ERROR_SERVICE_ERROR, "Failed to run task: Service did not start");
            }
        }
        catch(Exception e){
            promise.reject(ERROR_SERVICE_ERROR, "Failed to run task: " + e.getMessage());
        }
    }

    @ReactMethod
    public void isRunning(Promise promise) {
        // Get the ForegroundService running value
        ForegroundService instance = ForegroundService.getInstance();
        int res = 0;
        if(instance != null){
            res = instance.isRunning();
        }

        promise.resolve(res);
    }

    @ReactMethod
    public void checkNotificationPermission(Promise promise) {
        boolean hasPermission = hasNotificationPermission();
        promise.resolve(hasPermission);
    }
}