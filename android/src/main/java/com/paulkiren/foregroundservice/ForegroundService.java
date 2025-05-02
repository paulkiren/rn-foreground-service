package com.paulkiren.foregroundservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import androidx.annotation.Nullable;

public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";

    // Constants moved from Constants.java
    public static final String NOTIFICATION_CONFIG = "com.paulkiren.foregroundservice.notif_config";
    public static final String TASK_CONFIG = "com.paulkiren.foregroundservice.task_config";

    public static final String ACTION_FOREGROUND_SERVICE_START = "com.paulkiren.foregroundservice.service_start";
    public static final String ACTION_FOREGROUND_SERVICE_STOP = "com.paulkiren.foregroundservice.service_stop";
    public static final String ACTION_FOREGROUND_SERVICE_STOP_ALL = "com.paulkiren.foregroundservice.service_all";
    public static final String ACTION_FOREGROUND_RUN_TASK = "com.paulkiren.foregroundservice.service_run_task";
    public static final String ACTION_UPDATE_NOTIFICATION = "com.paulkiren.foregroundservice.service_update_notification";

    private static ForegroundService mInstance = null;
    private static Bundle lastNotificationConfig = null;
    private int running = 0;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnableCode;
    
    // Foreground service type - default to "data" for compatibility
    private int foregroundServiceType = -1;

    public static boolean isServiceCreated() {
        try {
            return mInstance != null && mInstance.ping();
        } catch (NullPointerException e) {
            Log.e(TAG, "Error checking if service is created", e);
            return false;
        }
    }

    public static ForegroundService getInstance() {
        if (isServiceCreated()) {
            return mInstance;
        }
        return null;
    }

    public int isRunning() {
        return running;
    }

    private boolean ping() {
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = 0;
        mInstance = this;
        Log.d(TAG, "Foreground service created");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Foreground service destroyed");
        if (runnableCode != null) {
            handler.removeCallbacks(runnableCode);
        }
        running = 0;
        mInstance = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            Log.e(TAG, "Invalid intent or action.");
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        switch (action) {
            case ACTION_FOREGROUND_SERVICE_START:
                handleStartService(extras);
                break;

            case ACTION_UPDATE_NOTIFICATION:
                handleUpdateNotification(extras);
                break;

            case ACTION_FOREGROUND_RUN_TASK:
                handleRunTask(extras);
                break;

            case ACTION_FOREGROUND_SERVICE_STOP:
                handleStopService();
                break;

            case ACTION_FOREGROUND_SERVICE_STOP_ALL:
                handleStopAllServices();
                break;

            default:
                Log.e(TAG, "Unknown action: " + action);
                break;
        }

        return START_STICKY;
    }

    private void handleStartService(Bundle extras) {
        if (extras == null || !extras.containsKey(NOTIFICATION_CONFIG)) {
            Log.e(TAG, "Missing notification config.");
            return;
        }

        Bundle notificationConfig = extras.getBundle(NOTIFICATION_CONFIG);
        startForegroundService(notificationConfig);
    }

    private void handleUpdateNotification(Bundle extras) {
        if (extras == null || !extras.containsKey(NOTIFICATION_CONFIG)) {
            Log.e(TAG, "Missing notification config for update.");
            return;
        }

        Bundle notificationConfig = extras.getBundle(NOTIFICATION_CONFIG);

        if (running <= 0) {
            Log.d(TAG, "Service not running, restarting with new notification.");
            startForegroundService(notificationConfig);
        } else {
            updateNotification(notificationConfig);
        }
    }

    private void handleRunTask(Bundle extras) {
        if (running <= 0 && lastNotificationConfig == null) {
            Log.e(TAG, "Service not running to execute tasks.");
            stopForegroundAndSelf(true);
            return;
        }

        if (running <= 0) {
            Log.d(TAG, "Restarting service for task execution.");
            if (!startForegroundService(lastNotificationConfig)) {
                Log.e(TAG, "Failed to restart service for task execution.");
                return;
            }
        }

        if (extras != null && extras.containsKey(TASK_CONFIG)) {
            Bundle taskConfig = extras.getBundle(TASK_CONFIG);
            executeTask(taskConfig);
        }
    }

    private void executeTask(Bundle taskConfig) {
        try {
            if (taskConfig.getBoolean("onLoop", false)) {
                // Remove previous runnable if it exists
                if (runnableCode != null) {
                    handler.removeCallbacks(runnableCode);
                }

                runnableCode = new Runnable() {
                    @Override
                    public void run() {
                        runHeadlessTask(taskConfig);
                        handler.postDelayed(this, taskConfig.getInt("loopDelay", 1000));
                    }
                };
                handler.post(runnableCode);
            } else {
                runHeadlessTask(taskConfig);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to execute task: " + e.getMessage(), e);
        }
    }
    
    private void handleStopService() {
        if (running > 0) {
            running--;
            if (running == 0) {
                stopForegroundAndSelf(true);
                lastNotificationConfig = null;
            }
        } else {
            Log.d(TAG, "Service not running to stop.");
            stopForegroundAndSelf(true);
            lastNotificationConfig = null;
        }
    }

    private void handleStopAllServices() {
        running = 0;
        mInstance = null;
        lastNotificationConfig = null;
        stopForegroundAndSelf(true);
    }
    
    private void stopForegroundAndSelf(boolean removeNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(removeNotification ? STOP_FOREGROUND_REMOVE : STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(removeNotification);
        }
        stopSelf();
    }

    private boolean startForegroundService(Bundle notificationConfig) {
        if (!NotificationHelper.hasNotificationPermission(getApplicationContext())) {
            Log.e(TAG, "Cannot start foreground service without notification permission on Android 13+");
            return false;
        }

        try {
            int id = (int) notificationConfig.getDouble("id");
            Notification notification = NotificationHelper
                    .getInstance(getApplicationContext())
                    .buildNotification(getApplicationContext(), notificationConfig);

            // Set the appropriate foreground service type
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                String serviceType = notificationConfig.getString("serviceType", "default");
                determineForegroundServiceType(serviceType);
                
                if (foregroundServiceType != -1) {
                    startForeground(id, notification, foregroundServiceType);
                } else {
                    startForeground(id, notification);
                }
            } else {
                startForeground(id, notification);
            }

            running++;
            lastNotificationConfig = notificationConfig;
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to start service: " + e.getMessage(), e);
            return false;
        }
    }

    @android.annotation.SuppressLint("InlinedApi")
    private void determineForegroundServiceType(String serviceType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            switch (serviceType.toLowerCase()) {
                case "location":
                    foregroundServiceType = android.app.Service.FOREGROUND_SERVICE_TYPE_LOCATION;
                    break;
                case "camera":
                    foregroundServiceType = android.app.Service.FOREGROUND_SERVICE_TYPE_CAMERA;
                    break;
                case "microphone":
                    foregroundServiceType = android.app.Service.FOREGROUND_SERVICE_TYPE_MICROPHONE;
                    break;
                case "mediaplayback":
                    foregroundServiceType = android.app.Service.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
                    break;
                case "datasynch":
                    foregroundServiceType = android.app.Service.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
                    break;
                default:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // For Android 12+ default to DATA_SYNC which is more permissive
                        foregroundServiceType = android.app.Service.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
                    } else {
                        foregroundServiceType = -1; // Use default
                    }
                    break;
            }
        }
    }

    private void updateNotification(Bundle notificationConfig) {
        try {
            int id = (int) notificationConfig.getDouble("id");
            Notification notification = NotificationHelper
                .getInstance(getApplicationContext())
                .buildNotification(getApplicationContext(), notificationConfig);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(id, notification);

            lastNotificationConfig = notificationConfig;
        } catch (Exception e) {
            Log.e(TAG, "Failed to update notification: " + e.getMessage(), e);
        }
    }

    public void runHeadlessTask(Bundle bundle) {
        Intent service = new Intent(getApplicationContext(), ForegroundServiceTask.class);
        service.putExtras(bundle);

        int delay = (int) bundle.getDouble("delay", 0);

        if (delay <= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Oreo+ we need to use startForegroundService due to background restrictions
                try {
                    getApplicationContext().startForegroundService(service);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting foreground service: " + e.getMessage(), e);
                }
            } else {
                try {
                    getApplicationContext().startService(service);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting service: " + e.getMessage(), e);
                }
            }
        } else {
            handler.postDelayed(() -> {
                if (running <= 0) {
                    return;
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getApplicationContext().startForegroundService(service);
                    } else {
                        getApplicationContext().startService(service);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to start delayed headless task: " + e.getMessage(), e);
                }
            }, delay);
        }
    }

    // Inner class for headless task
    public static class ForegroundServiceTask extends HeadlessJsTaskService {
        @Nullable
        @Override
        protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                return new HeadlessJsTaskConfig(
                    extras.getString("taskName", "ForegroundServiceTask"),
                    Arguments.fromBundle(extras),
                    extras.getInt("timeout", 5000), // timeout for the task (default 5s)
                    extras.getBoolean("allowInForeground", true) // allow in foreground
                );
            }
            return null;
        }
    }
}