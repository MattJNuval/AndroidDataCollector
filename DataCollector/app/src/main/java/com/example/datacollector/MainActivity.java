package com.example.datacollector;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Location currentLocation;
    private Geocoder geocoder;
    private Location lastLocation;
    private List<Float> lightValues;
    private Sensor lightSensor;
    private SensorEventListener lightSensorListener;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Resources res;
    private SensorManager sensorManager;

    private final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
    };


    @SuppressLint("WifiManagerLeak")
    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

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


    private void resetLastLocation(Location loc) {
        this.lastLocation = null;
        if (loc != null) {
            this.lastLocation = new Location("Point A");
            this.lastLocation.setAltitude(loc.getAltitude());
            this.lastLocation.setLatitude(loc.getLatitude());
            this.lastLocation.setLongitude(loc.getLongitude());
        }
        return;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                System.out.println("CURRENT VALUES \n"+ "Longitude: " + longitude + "\n"
                        + "Latitude: " + latitude + "\n"
                        + "Altitude: " + altitude + "\n"
                        + "Location Name: " + locationName +"\n"
                        + "WIFI SPEED: " + info);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


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
        if (this.lightSensor != null) {
            this.sensorManager.registerListener(this.lightSensorListener, this.lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
        if (this.lightSensorListener != null) {
            this.sensorManager.unregisterListener(this.lightSensorListener);
        }
        return;
    }
}


