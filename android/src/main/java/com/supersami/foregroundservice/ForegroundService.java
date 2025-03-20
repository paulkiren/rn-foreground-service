package com.supersami.foregroundservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;

import static com.supersami.foregroundservice.Constants.NOTIFICATION_CONFIG;
import static com.supersami.foregroundservice.Constants.TASK_CONFIG;


// NOTE: headless task will still block the UI so don't do heavy work, but this is also good
// since they will share the JS environment
// Service will also be a singleton in order to quickly find out if it is running

public class ForegroundService extends Service {

    private static ForegroundService mInstance = null;
    private static Bundle lastNotificationConfig = null;
    private int running = 0;



    public static boolean isServiceCreated(){
        try{
            return mInstance != null && mInstance.ping();
        }
        catch(NullPointerException e){
            return false;
        }
    }

    public static ForegroundService getInstance(){
        if(isServiceCreated()){
            return mInstance;
        }
        return null;
    }

    public int isRunning(){
        return running;
    }

    private boolean ping(){
        return true;
    }

    @Override
    public void onCreate() {
        //Log.e("ForegroundService", "destroy called");
        running = 0;
        mInstance = this;
    }

    @Override
    public void onDestroy() {
        //Log.e("ForegroundService", "destroy called");
        this.handler.removeCallbacks(this.runnableCode);
        running = 0;
        mInstance = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean startService(Bundle notificationConfig){
        try {
            int id = (int)notificationConfig.getDouble("id");

            Notification notification = NotificationHelper
                .getInstance(getApplicationContext())
                .buildNotification(getApplicationContext(), notificationConfig);

            // Ensure the service starts in the foreground
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!NotificationHelper.hasNotificationPermission(this)) {
                    Log.e("ForegroundService", "Notification permission is required for Android 12+.");
                    return false;
                }
            }

            startForeground(id, notification);

            running += 1;

            lastNotificationConfig = notificationConfig;

            return true;

        }
        catch (Exception e) {
            Log.e("ForegroundService", "Failed to start service: " + e.getMessage());
            return false;
        }
    }
    public  Bundle taskConfig;
    private Handler handler = new Handler();
    private Runnable runnableCode = new Runnable() {
      @Override
      public void run() {
        final Intent service = new Intent(getApplicationContext(), ForegroundServiceTask.class);
        service.putExtras(taskConfig);
        getApplicationContext().startService(service);

        int delay = (int)taskConfig.getDouble("delay");

          int loopDelay = (int)taskConfig.getDouble("loopDelay");
          Log.d("SuperLog",""+loopDelay);
        handler.postDelayed(this, loopDelay);
      }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            Log.e("ForegroundService", "Invalid intent or action.");
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        switch (action) {
            case Constants.ACTION_FOREGROUND_SERVICE_START:
                handleStartService(extras);
                break;

            case Constants.ACTION_UPDATE_NOTIFICATION:
                handleUpdateNotification(extras);
                break;

            case Constants.ACTION_FOREGROUND_RUN_TASK:
                handleRunTask(extras);
                break;

            case Constants.ACTION_FOREGROUND_SERVICE_STOP:
                handleStopService();
                break;

            case Constants.ACTION_FOREGROUND_SERVICE_STOP_ALL:
                handleStopAllServices();
                break;

            default:
                Log.e("ForegroundService", "Unknown action: " + action);
                break;
        }

        return START_REDELIVER_INTENT;
    }

    private void handleStartService(Bundle extras) {
        if (extras == null || !extras.containsKey(NOTIFICATION_CONFIG)) {
            Log.e("ForegroundService", "Missing notification config.");
            return;
        }

        Bundle notificationConfig = extras.getBundle(NOTIFICATION_CONFIG);
        startService(notificationConfig);
    }

    private void handleUpdateNotification(Bundle extras) {
        if (extras == null || !extras.containsKey(NOTIFICATION_CONFIG)) {
            Log.e("ForegroundService", "Missing notification config for update.");
            return;
        }

        Bundle notificationConfig = extras.getBundle(NOTIFICATION_CONFIG);

        if (running <= 0) {
            Log.d("ForegroundService", "Service not running, restarting with new notification.");
            startService(notificationConfig);
        } else {
            updateNotification(notificationConfig);
        }
    }

    private void handleRunTask(Bundle extras) {
        if (running <= 0 && lastNotificationConfig == null) {
            Log.e("ForegroundService", "Service not running to execute tasks.");
            stopSelf();
            return;
        }

        if (running <= 0) {
            Log.d("ForegroundService", "Restarting service for task execution.");
            if (!startService(lastNotificationConfig)) {
                Log.e("ForegroundService", "Failed to restart service for task execution.");
                return;
            }
        }

        if (extras != null && extras.containsKey(TASK_CONFIG)) {
            taskConfig = extras.getBundle(TASK_CONFIG);
            executeTask(taskConfig);
        }
    }

    private void executeTask(Bundle taskConfig) {
        try {
            if (taskConfig.getBoolean("onLoop", false)) {
                handler.post(runnableCode);
            } else {
                runHeadlessTask(taskConfig);
            }
        } catch (Exception e) {
            Log.e("ForegroundService", "Failed to execute task: " + e.getMessage());
        }
    }

    private void handleStopService() {
        if (running > 0) {
            running--;
            if (running == 0) {
                stopSelf();
                lastNotificationConfig = null;
            }
        } else {
            Log.d("ForegroundService", "Service not running to stop.");
            stopSelf();
            lastNotificationConfig = null;
        }
    }

    private void handleStopAllServices() {
        running = 0;
        mInstance = null;
        lastNotificationConfig = null;
        stopSelf();
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
            Log.e("ForegroundService", "Failed to update notification: " + e.getMessage());
        }
    }

    public void runHeadlessTask(Bundle bundle){
        final Intent service = new Intent(getApplicationContext(), ForegroundServiceTask.class);
        service.putExtras(bundle);

        int delay = (int)bundle.getDouble("delay");

        if(delay <= 0){
            getApplicationContext().startService(service);

            // wakelock should be released automatically by the task
            // Shouldn't be needed, it's called automatically by headless
            //HeadlessJsTaskService.acquireWakeLockNow(getApplicationContext());
        }
        else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(running <= 0){
                        return;
                    }
                    try{
                        getApplicationContext().startService(service);
                    }
                    catch (Exception e) {
                        Log.e("ForegroundService", "Failed to start delayed headless task: " + e.getMessage());
                    }
                }
            }, delay);
        }


    }
}