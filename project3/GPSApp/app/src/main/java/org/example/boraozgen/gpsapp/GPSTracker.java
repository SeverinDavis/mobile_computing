package org.example.boraozgen.gpsapp;

import android.app.Service;
import android.location.LocationListener;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
// IO related imports
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Environment;

/* GPS tracker service, which runs in the background and
   obtains location information. It provides the
   ITrackerService interface to the MainActivity
 */
public class GPSTracker extends Service {

    private static final String TAG = "GPSTracker";

    // Current location values
    double latitude;
    double longitude;
    double distance;
    double avgSpeed;
    // First location: for distance calculation
    Location firstLocation;
    // Array of locations: for location logging
    List<Location> locationPoints;
    // Location Manager: the system service, which provides
    // the location information to the GPSTracker service
    LocationManager locationManager;
    // Listener for location callbacks
    LocationListener listener;
    // GPX file creator: defined below
    GPX navigation;

    // TODO: possible feature: ask for permissions

    /* Constructor */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "GPSTracker object created.");

        // Instantiate everything
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationPoints = new ArrayList<Location>();
        navigation = new GPX();

        // Implementation of listener for location callbacks
        listener = new LocationListener() {
            // Called when the device location is changed
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "Location changed");

                // Entered only at the first time
                if (firstLocation == null) {
                    Log.d(TAG, "First location obtained");

                    // Instantiate firstLocation and fill it
                    firstLocation = new Location("");
                    firstLocation.setLatitude(location.getLatitude());
                    firstLocation.setLongitude(location.getLongitude());
                }

                // Add location values to the list
                locationPoints.add(location);
                // Update location values
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                distance = location.distanceTo(firstLocation);
                avgSpeed = location.getSpeed();
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

    @SuppressWarnings("MissingPermission")
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "GPSTracker service started.");

        /*
         * Time between updates: 5000 ms
         * Distance between updates: 1 meter
         */
        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 5000, 1, listener);
        // TODO: Use GPS_PROVIDER. (does not work with my Nexus 5) Also: updates every ~20 seconds instead of 5

        return Service.START_STICKY;
    }

    /* Implementation of AIDL interface */
    private final ITrackerService.Stub mBinder = new ITrackerService.Stub() {
        @Override
        public double getLatitude(){
            return latitude;
        }
        @Override
        public double getLongitude() {
            return longitude;
        }
        @Override
        public double getDistance() {
            return distance;
        }
        @Override
        public double getSpeed() {
            return avgSpeed;
        }
    };

    /* Returns the interface stub when bound */
    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    /* Destructor */
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "GPSTracker service stopped.");

        // Stop location manager updates
        locationManager.removeUpdates(listener);

        // Save list to the file
        if (locationPoints != null) {

            // Get external storage directory
            String FilePath = Environment.getExternalStorageDirectory().toString() + "/navigation.gpx";
            File file = new File(FilePath);

            if (file.exists()) {
                Log.d(TAG, "File exists, deleting...");
                file.delete();
            }

            try {
                Log.d(TAG, "Creating file...");
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            navigation.writePath(file, "Navigation", locationPoints);
        }

    }

    public class GPX {
        private final String TAG = GPX.class.getName();

        // Writes the location points to the file in the GPX format
        public void writePath(File file, String n, List<Location> points) {

            String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
            String name = "<name>" + n + "</name><trkseg>\n";

            String segments = "";
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            for (Location l : points) {
                segments += "<trkpt lat=\"" + l.getLatitude() + "\" lon=\"" + l.getLongitude() + "\"><time>" + df.format(new Date(l.getTime())) + "</time></trkpt>\n";
            }

            String footer = "</trkseg></trk></gpx>";

            try {
                FileWriter writer = new FileWriter(file, true);
                writer.append(header);
                writer.append(name);
                writer.append(segments);
                writer.append(footer);
                writer.flush();
                writer.close();

            } catch (IOException e) {
                Log.e(TAG, "Error Writting Path",e);
            }
        }
    }
}