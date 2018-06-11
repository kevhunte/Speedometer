package kevinhunte.speedometer;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

import java.util.ArrayList;

public class TransitionIntentService extends IntentService {

    private static final String TAG = TransitionIntentService.class.getSimpleName();
    public String status = "Nothing Yet ";
    private String state = "entered ";

    public TransitionIntentService() {
        super("TransitionIntentService");
    }

    /** Debugging purposes. Intent Service still not working on actual phone*/
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"Service created");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG,"Memory is low");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"Service destroyed");
    }
    /** ------------------------------------------------------------ */

    @Override
    protected void onHandleIntent(Intent intent) {
            if(ActivityTransitionResult.hasResult(intent)){
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
                    broadcastEvent(event);//Sends info to main thread
                    //Log.e(TAG, "User: "+state+" "+status);
                }
            }else{
                Log.e(TAG,"Problem. No Act Trans result.\nValue of cond: " +ActivityTransitionResult.hasResult(intent)+
                        "\nIntent action: "+intent.getAction()+"\nType: "+intent.getType());//debugging
            }

    }

    private void broadcastEvent(ActivityTransitionEvent event) {//sends data over
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
