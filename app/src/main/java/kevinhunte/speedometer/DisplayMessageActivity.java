package kevinhunte.speedometer;

import android.Manifest;
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

public class DisplayMessageActivity extends AppCompatActivity {

    //found from a YouTube video
    private LocationManager locationManager;//used to access gps
    private LocationListener locationListener;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {//import needed functions for gps, use to make function and return speed
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        textView = (TextView) findViewById(R.id.textView);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {//if the gps location changes
                textView.append("\n"+location.getLatitude()+ " "+location.getLongitude());

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

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);//update location every second


    }

    public void exitComm(View view){//exit returns to the main screen/activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }





    //then print output on xml file. Do in while loop

}
