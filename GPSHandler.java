package com.example.coht;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
public class GPSHandler {
    private static final long MIN_TIME = 0;
    private static final float MIN_DISTANCE = 0;
    private static final int MAX_RESULTS = 1;

    private Context mContext;
    private LocationManager mLocationManager;
    private Geocoder mGeocoder;
    private String currentAddress;
    private List<String> hospitalAddresses;

    private List<Point> mPoints;
    private List<String> items;

    public String getCurrentAddress() {
        return currentAddress;
    }

    public List<String> getHospitalAddress() { return hospitalAddresses; }

    public GPSHandler(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
        mGeocoder = new Geocoder(mContext);    // Object to get address using coordinates

        mPoints = new ArrayList<>();
        hospitalAddresses = new ArrayList<>();
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("debug", "location changed");
            findCurrentAddress(location);
            findHospitalAddress(location);

            /*
             * Code for calculation Speed
            Point mPoint = new Point(location.getLongitude(), location.getLatitude(), System.currentTimeMillis());
            mPoints.add(mPoint);
            calcSpeed();
             */
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }
    };

    private void findCurrentAddress(Location location) {
        List<Address> addresses = null;                 // To hold the location and hospital addresses
        try {
            addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), MAX_RESULTS);
        } catch (IOException e) {
        }

        currentAddress = "";
        if (addresses.size() > 0) {
            currentAddress = addresses.get(0).getAddressLine(0);
        }
    }

    private void findHospitalAddress(Location location) {
        final String locationParm = location.getLatitude() + "," + location.getLongitude();
        new Thread(new Runnable() {

            @Override
            public void run() {
                InputStream inputStream = null;
                HttpURLConnection urlConnection = null;
                try {
                    // Connect to Google API Services
                    URL url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + locationParm + "&radius=5000&types=hospital&key=AIzaSyCaQth6Pfz0pMkAmxDs-Pgx6IpSmpz0bwY");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();

                    // Get the data from the API
                    inputStream = urlConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuffer stringBuffer = new StringBuffer();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) stringBuffer.append(line);

                    // Parse data and convert it to a JSON Object
                    JSONObject jsonObject = new JSONObject((stringBuffer.toString()));
                    JSONArray results = jsonObject.getJSONArray("results"); // Returns the hospitals found

                    hospitalAddresses.clear();
                    for (int ii = 0; ii < results.length(); ++ii) {
                        JSONObject jsonObjectEachResult = results.getJSONObject(ii);

                        String name = jsonObjectEachResult.optString("name");
                        String vicinity = jsonObjectEachResult.optString("vicinity");

                        hospitalAddresses.add(name + " at " + vicinity);
                    }
                } catch (Exception ex) {}
            }
        }).start();
    }
}
