package kevinhunte.speedometer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

import static android.app.PendingIntent.getActivity;
import static kevinhunte.speedometer.MainActivity.CHANNEL_ID;

//changed from IntentService
public class TransitionIntentService extends IntentService {

    private static final String TAG = TransitionIntentService.class.getSimpleName();
    static final int JOB_ID = 50;
    public String status = "Nothing Yet ";
    private String state = "entered ";
    private Notification notification;


    public TransitionIntentService() {
        super(TAG);
    }

    /** Debugging purposes. Intent Service still not working on actual phone*/


    private void makeNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "TransitionIntentService")
                .setContentTitle("Speedometer is Running")
                .setContentText("Google Activity Recognition is being used")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"Service created");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG,"Memory is low");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"Service destroyed");
    }
    /** ------------------------------------------------------------ */

    /**
    public static void enqueueWork(Context ctx, Intent intent) {
        Log.e(TAG,"enqueueWork called");
        enqueueWork(ctx, TransitionIntentService.class, JOB_ID, intent);
    }*/

    @Override
    protected void onHandleIntent(@NonNull Intent intent) {
        if(ActivityTransitionResult.hasResult(intent)){
            Log.e(TAG,"Working, getting result");
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()){//display each event
                switch (event.getActivityType()){//will this work?
                    case 7: {
                        status = "walking";
                        //7 for walking and 8 for running
                        //Log.e(TAG, "Activity Type: " + event.getActivityType()+" Transition Type: "+ event.getTransitionType());
                        break;
                    }
                    case 8: {
                        status = "running";
                        //Log.e(TAG, "Activity Type: " + event.getActivityType()+" Transition Type: "+ event.getTransitionType());
                        break;
                    }
                    case 3: {
                        status = "still";
                        //Log.e(TAG, "Activity Type: " + event.getActivityType()+" Transition Type: "+ event.getTransitionType());
                        break;
                    }
                    default: {
                        status = "unknown";
                        //Log.e(TAG, "Activity Type: " + event.getActivityType()+" Transition Type: "+ event.getTransitionType());
                        break;
                    }
                }
                if(event.getTransitionType()==1) state="exited ";//1 is EXIT, 0 is ENTER
                broadcast();//Sends info to main thread
                Log.d(TAG, "User: "+state+" "+status);
            }
        }else{
            Log.e(TAG,"Problem. No Act Trans result.\nValue of intent: " +ActivityTransitionResult.hasResult(intent)+
                    "\nIntent data: "+intent.getDataString()+"\nType: "+intent.getType());//debugging
        }
    }

    private void broadcast() {//sends data over
        Intent intent = new Intent("activityRec_intent");
        intent.putExtra("type",status);
        intent.putExtra("transition", state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

/**
    private void broadcastActivity(DetectedActivity activity) {
        Intent intent = new Intent("activity_intent");
        intent.putExtra("type", activity.getType());
        intent.putExtra("confidence", activity.getConfidence());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }*/


}
