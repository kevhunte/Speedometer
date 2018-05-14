package kevinhunte.speedometer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;//used to access gps
    private LocationListener locationListener;
    private TextView text;
    private TextView text2;
    private TextView text3;
    private float speed;
    private float max_speed;
    private float avg_speed;
    private float speed_sum;
    private float count;

    //private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.textView4);//ID of the widget to input speed value
        text2 = (TextView) findViewById(R.id.textView6);//ID of widget for max speed val
        text3 = (TextView) findViewById(R.id.textView8);//ID of widget for avg speed
        speed_sum=0;
        max_speed=0;//initialized at zero
        count=0;



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
                    text.setText(speed+" m/s");//sets this textView to now be the speed value
                }
                avg_speed = speed_sum/count;//sum of speeds over count of changes
                text2.setText(max_speed+" m/s");
                text3.setText(avg_speed+" m/s");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {//if gps is turned off, offer option to turn on
                Intent _intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);//goes to settings
                startActivity(_intent);
                //MAKE LESS SUDDEN
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

}
