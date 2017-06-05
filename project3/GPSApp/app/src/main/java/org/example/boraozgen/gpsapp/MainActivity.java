package org.example.boraozgen.gpsapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.RemoteException;
import android.util.Log;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = "MainActivity";

    // View elements
    Button btnUpdateValues;
    Button btnStartService;
    Button btnStopService;
    TextView latitudeView;
    TextView longitudeView;
    TextView distanceView;
    TextView avgSpeedView;

    // Current values
    double latitude;
    double longitude;
    double distance;
    double avgSpeed;

    // Interface to the service
    ITrackerService trackerService;

    /* Callback for connection to the service */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        trackerService = ITrackerService.Stub.asInterface(service);
        Log.d(MainActivity.TAG, "onServiceConnected() connected");
        Toast.makeText(MainActivity.this, "Service connected", Toast.LENGTH_LONG)
                .show();
    }

    /* Callback for disconnection from the service */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        trackerService = null;
        Log.d(MainActivity.TAG, "onServiceDisconnected() disconnected");
        Toast.makeText(MainActivity.this, "Service disconnected", Toast.LENGTH_LONG)
                .show();
    }

    /* Starts the service and binds this activity to the service. */
    private void initService() {
        Intent i = new Intent(this, GPSTracker.class);
        startService(i);
        boolean ret = bindService(i, this, 0);
        Log.d(TAG, "initService() bound with " + ret);
    }

    /* Unbinds this activity from the service. */
    private void releaseService() {
        Intent i = new Intent(this, GPSTracker.class);
        stopService(i);
        Log.d(TAG, "releaseService() unbound.");
    }

    /* Constructor */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the view elements
        btnUpdateValues = (Button) findViewById(R.id.btnUpdateValues);
        btnStartService = (Button) findViewById(R.id.btnStartService);
        btnStopService = (Button) findViewById(R.id.btnStopService);
        latitudeView = (TextView) findViewById(R.id.latitudeView);
        longitudeView = (TextView) findViewById(R.id.longitudeView);
        distanceView = (TextView) findViewById(R.id.distanceView);
        avgSpeedView = (TextView) findViewById(R.id.avgSpeedView);

        // Update values button click event
        btnUpdateValues.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Check if the service has been started
                if (trackerService!=null) {
                    try {
                        // Get current values
                        latitude = trackerService.getLatitude();
                        longitude = trackerService.getLongitude();
                        avgSpeed = trackerService.getSpeed();
                        distance = trackerService.getDistance();
                    } catch (RemoteException e) {
                        Log.d(MainActivity.TAG, "onClick failed with: " + e);
                        e.printStackTrace();
                    }
                    // Set text values on the screen
                    latitudeView.setText(Double.toString(latitude) + " °");
                    longitudeView.setText(Double.toString(longitude) + " °");
                    avgSpeedView.setText(Double.toString(avgSpeed) + " m/s");
                    distanceView.setText(Double.toString(distance) + " m");
                }
            }
        });

        // Start service button click event
        btnStartService.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                initService();
            }
        });

        // Stop Service
        btnStopService.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                releaseService();
                latitudeView.setText("");
                longitudeView.setText("");
                avgSpeedView.setText("");
                distanceView.setText("");
            }
        });
    }

    /* Called when the activity is about to be destroyed. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseService();
        // TODO: bug: rotating the screen causes the release of the service
    }
}