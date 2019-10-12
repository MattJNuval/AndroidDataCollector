package com.example.datacollector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Location currentLocation;
    private Geocoder geocoder;
    private Location lastLocation;;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Resources res;
    private SensorManager sensorManager;
    private TextView data;

    private final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private StringBuilder dataStr = new StringBuilder();


    private String computeLocationName(Location loc) {
        try {
            final List<Address> addresses = this.geocoder.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                return addresses.get(0).getAddressLine(0);
            }
        }
        catch (IOException e) {
            Log.e("LOCATION", "Could not get location!");
            e.printStackTrace();
        }
        catch (Exception e) {
            Log.e("LOCATION", "Could not get location!");
            e.printStackTrace();
        }
        return "NaN";
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = (TextView)findViewById(R.id.data);
        dataStr.append("Link Speed,Longitude,Latitude,Altitude,Street,City,State And Zip,Country");

        @SuppressLint("WifiManagerLeak")
        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        this.currentLocation = new Location("Point B");
        this.geocoder = new Geocoder(this, Locale.getDefault());
        this.locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        this.res = getResources();
        this.sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        if (ContextCompat.checkSelfPermission(this, this.PERMISSIONS[0])
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, this.PERMISSIONS[1])
                        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    this.PERMISSIONS[0])
                    && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    this.PERMISSIONS[1])) {
                Log.w("PERMISSION", "Requesting permissions!");
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
                Log.w("PERMISSION", "Requesting permissions!");
            }
        }

        this.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                int info = wifi.getConnectionInfo().getLinkSpeed();

                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();
                final double altitude = location.getAltitude();

                currentLocation.setLongitude(longitude);
                currentLocation.setLatitude(latitude);
                currentLocation.setAltitude(altitude);

                final String locationName = computeLocationName(currentLocation);

                data.setText("CURRENT VALUES \n"+ "Longitude: " + longitude + "\n"
                        + "Latitude: " + latitude + "\n"
                        + "Altitude: " + altitude + "\n"
                        + "Location Name: " + locationName +"\n"
                        + "WIFI SPEED: " + info);
                dataStr.append("\n" + String.valueOf(info) + ',' + String.valueOf(longitude) + "," + String.valueOf(latitude) + "," + String.valueOf(altitude) + "," + String.valueOf(locationName));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                return;
            }

            @Override
            public void onProviderEnabled(String provider) {
                return;
            }

            @Override
            public void onProviderDisabled(String provider) {
                return;
            }
        };


    }


    public void export(View view) {
        try {
            FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
            out.write((dataStr.toString()).getBytes());
            out.close();

            Context context = getApplicationContext();
            File fileLocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context,"com.example.datacollector.fileprovider", fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent,"Sent Mail"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[0])
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[1])
                == PackageManager.PERMISSION_GRANTED) {
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    500, 1, this.locationListener);
            this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    500, 1, this.locationListener);
        }
        return;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[0])
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[1])
                == PackageManager.PERMISSION_GRANTED) {
            this.locationManager.removeUpdates(locationListener);
        }
        return;
    }
}




