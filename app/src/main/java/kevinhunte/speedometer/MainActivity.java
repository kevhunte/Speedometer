package kevinhunte.speedometer;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;//activity recognition
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;


public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    private String message;
    private LocationManager locationManager;//used to access gps
    private LocationListener locationListener;
    private TextView text;
    private TextView text2;
    private TextView text3;
    public TextView textactivity;
    private Button Rec_on;
    private Button Rec_off;
    private float speed;
    private float max_speed;
    private float avg_speed;
    private float speed_sum;
    private float count;
    private List<ActivityTransition> transitions;
    private ActivityRecognitionClient activityRecognitionClient;
    private PendingIntent transitionPendingIntent;
    private Context mContext;

    //private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.textView4);//ID of the widget to input speed value
        text2 = findViewById(R.id.textView6);//ID of widget for max speed val
        text3 = findViewById(R.id.textView8);//ID of widget for avg speed
        textactivity = findViewById(R.id.textView2);//ID for activity rec
        Rec_on = findViewById(R.id.button2);
        Rec_off = findViewById(R.id.button3);
        message ="";
        speed_sum=0;
        max_speed=0;//initialized at zero
        count=0;

        /** INIT ACTIVITY RECOGNITION **/
        mContext=this;//link context with info from this class
        activityRecognitionClient=ActivityRecognition.getClient(mContext);
        Intent intent = new Intent(this, TransitionIntentService.class);
        transitionPendingIntent = PendingIntent.getService(this,100,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        transitions = new ArrayList<>();
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());

        //registers broadcast receiver made. Will only receive information from thread running in Trans intent service
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter("activityRec_intent"));
        /** ---------------- **/

        /** GPS Speedometer Logic **/
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {//if the gps location changes
                speed = location.getSpeedAccuracyMetersPerSecond();//returns speed
                speed_sum+=speed;//adds all speeds together
                count++;
                if(speed>max_speed){ //sets max speed
                    max_speed = speed;
                }
                if(speed<0.9){//will update when user is no longer moving. Too sensitive to ever get zero
                    text.setText("Not Moving");
                }else {
                    text.setText("Current Speed: "+speed+" m/s");//sets this textView to now be the speed value
                }
                avg_speed = speed_sum/count;//sum of speeds over count of changes
                text2.setText("Max Speed: "+max_speed+" m/s");
                text3.setText("Avg Speed: "+avg_speed+" m/s");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {//if gps is turned off, offer option to turn on
                Toast.makeText(mContext,"Speedometer App needs GPS",Toast.LENGTH_LONG).show();
                Intent _intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);//goes to settings
                startActivity(_intent);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{//permissions needed for gps
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    //Manifest.permission.ACCESS_COARSE_LOCATION,
                    //Manifest.permission.INTERNET
            }, 5);//5 is arbitrary
            return;
        }

        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);//updates location as frequently as possible
        /** --------------- **/
        registerHandler();//calls activity rec on creation of window
    }

    /** member var for receiving act rec*/
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {//gets data from Activity Transition
        @Override
        public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra("type");
                String transition = intent.getStringExtra("transition");
                Log.i(LOG_TAG,"Broadcast Received: Activity Type: "+type+" Transition: "+transition);
                textactivity.setText("User "+transition+type+" position");
        }
    };

    public void registerHandler() {//add View View to start by widget
        final ActivityTransitionRequest activityTransitionRequest = new ActivityTransitionRequest(transitions);//list activities added above
        Task<Void> task = activityRecognitionClient.requestActivityTransitionUpdates(activityTransitionRequest,transitionPendingIntent);//keeps connection alive
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                /**Start tracking not needed to run on emulator, makes second thread*/
                //startTracking();//creates intent to call service
                Toast.makeText(mContext,"Transition Rec On",Toast.LENGTH_LONG).show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext,"Transition rec didn't work",Toast.LENGTH_LONG).show();
                e.printStackTrace();//print error
            }
        });
    }


    public void deregisterHandler(View view) {
        Task<Void> task = activityRecognitionClient.removeActivityTransitionUpdates(transitionPendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                transitionPendingIntent.cancel();
                /**Start tracking not needed to run on emulator, makes second thread. Neither is stop tracking*/
                //stopTracking();
                Toast.makeText(mContext, "Activity Transition Off", Toast.LENGTH_LONG).show();

            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext, "Remove Activity Transition Failed", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    /**Activates activity rec*/
    private void startTracking() {
        Intent intent = new Intent(this, TransitionIntentService.class);
        startService(intent);
    }
    /**Turns it off*/
    private void stopTracking(){
        Intent intent = new Intent(this, TransitionIntentService.class);
        stopService(intent);
    }

    public void startMessage(View view){//action taken when start button is pressed.
        //View specifies that something was clicked
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        //Intents are used to bind windows, activities together
        //EditText editText = (EditText) findViewById(R.id.editText); //not working properly
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);//launches new intent, which opens new activity(window)
    }

    /** For testing purposes


    protected ArrayList<String> getPermissions()
    {
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissions;
    }

    protected boolean permissionGranted()
    {
        for (String permission : getPermissions())
        {//updated from method. checks writing permission at runtime. Returns -1 until granted by user.
            if (ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED)
            {
                    Log.d(LOG_TAG,"DEBUGGING. Truth value for checkSelfPermission: "+ContextCompat.checkSelfPermission(this,permission));
                    Log.d(LOG_TAG, "Missing permission (for data logging): " + permission);//why is it not recognized?
                throw new NullPointerException("Missing permission: " + permission);
            }
        }
        return true;
    }
    /** --------------- **/

}
