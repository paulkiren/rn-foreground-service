package com.supersami.foregroundservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
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

            // Android 14+ requires service type parameter
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // API 34+ (Android 14+)
                int serviceType = getServiceType(notificationConfig);
                startForeground(id, notification, serviceType);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29+ (Android 10+) - service type support added but not required
                int serviceType = getServiceType(notificationConfig);
                startForeground(id, notification, serviceType);
            } else {
                // API 28 and below - no service type parameter
                startForeground(id, notification);
            }

            running += 1;

            lastNotificationConfig = notificationConfig;

            return true;

        }
        catch (Exception e) {
            Log.e("ForegroundService", "Failed to start service: " + e.getMessage());
            return false;
        }
    }

    /**
     * Maps service type string from JavaScript to Android ServiceInfo constant
     * @param config Notification configuration bundle from JavaScript
     * @return ServiceInfo constant for the specified type, defaults to DATA_SYNC
     */
    private int getServiceType(Bundle config) {
        if (config == null || !config.containsKey("serviceType")) {
            // Default to dataSync if not specified
            return ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
        }

        String serviceType = config.getString("serviceType", "dataSync");

        switch (serviceType.toLowerCase()) {
            case "camera":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA;

            case "connecteddevice":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;

            case "datasync":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;

            case "health":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH;

            case "location":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;

            case "mediaplayback":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;

            case "mediaprojection":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;

            case "microphone":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;

            case "phonecall":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL;

            case "remotemessaging":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING;

            case "shortservice":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE;

            case "specialuse":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

            case "systemexempted":
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED;

            default:
                Log.w("ForegroundService", "Unknown service type: " + serviceType + ", defaulting to dataSync");
                return ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
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
        String action = intent.getAction();

        /**
        From the docs:
        Every call to this method will result in a corresponding call to the target service's
        Service.onStartCommand(Intent, int, int) method, with the intent given here.
        This provides a convenient way to submit jobs to a service without having to bind and call on to its interface.
        */

        //Log.d("ForegroundService", "onStartCommand flags: " + String.valueOf(flags) + "  " + String.valueOf(startId));

        if (action != null) {
            if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_START)) {
                if (intent.getExtras() != null && intent.getExtras().containsKey(NOTIFICATION_CONFIG)) {
                    Bundle notificationConfig = intent.getExtras().getBundle(NOTIFICATION_CONFIG);

                    startService(notificationConfig);

                }
            }

            if (action.equals(Constants.ACTION_UPDATE_NOTIFICATION)) {
                if (intent.getExtras() != null && intent.getExtras().containsKey(NOTIFICATION_CONFIG)) {
                    Bundle notificationConfig = intent.getExtras().getBundle(NOTIFICATION_CONFIG);

                    if(running <= 0){
                        Log.d("ForegroundService", "Update Notification called without a running service, trying to restart service.");
                        startService(notificationConfig);
                    }
                    else{

                        try {
                            int id = (int)notificationConfig.getDouble("id");

                            Notification notification = NotificationHelper
                                .getInstance(getApplicationContext())
                                .buildNotification(getApplicationContext(), notificationConfig);

                            NotificationManager mNotificationManager=(NotificationManager)getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
                            mNotificationManager.notify(id, notification);

                            lastNotificationConfig = notificationConfig;

                        }
                        catch (Exception e) {
                            Log.e("ForegroundService", "Failed to update notification: " + e.getMessage());
                        }
                    }

                }
            }

            else if (action.equals(Constants.ACTION_FOREGROUND_RUN_TASK)){
                if(running <= 0 && lastNotificationConfig == null){
                    Log.e("ForegroundService", "Service is not running to run tasks.");
                    stopSelf();
                    return START_NOT_STICKY;
                }
                else{

                    // try to re-start service if it was killed
                    if(running <= 0){
                        Log.d("ForegroundService", "Run Task called without a running service, trying to restart service.");
                        if(!startService(lastNotificationConfig)){
                            Log.e("ForegroundService", "Service is not running to run tasks.");
                            return START_REDELIVER_INTENT;
                        }
                    }

                    if (intent.getExtras() != null && intent.getExtras().containsKey(TASK_CONFIG)) {
                        taskConfig = intent.getExtras().getBundle(TASK_CONFIG);

                        try {

                             if( taskConfig.getBoolean("onLoop") == true) {
                                 this.handler.post(this.runnableCode);
                             }else{
                                 this.runHeadlessTask(taskConfig);
                             }


                        }
                        catch (Exception e) {
                            Log.e("ForegroundService", "Failed to start task: " + e.getMessage());
                        }
                    }
                }
            }

            else if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_STOP)) {
                if(running > 0){
                    running -= 1;

                    if (running == 0){
                        stopSelf();
                        lastNotificationConfig = null;
                    }
                }
                else{
                    Log.d("ForegroundService", "Service is not running to stop.");
                    stopSelf();
                    lastNotificationConfig = null;
                }
                return START_NOT_STICKY;

            }
            else if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_STOP_ALL)) {
                running = 0;
                mInstance = null;
                lastNotificationConfig = null;
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        // service to restart automatically if it's killed
        return START_REDELIVER_INTENT;

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