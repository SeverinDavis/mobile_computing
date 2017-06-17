package com.example.sever.mobilecomputingtask1;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sever.mobilecomputingtask1.R;

import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Math.exp;
import static java.lang.Math.log;

public class MainActivity extends AppCompatActivity {

    BluetoothManager bluetoothManager;
    ArrayList<BluetoothDevice> devicelist = new ArrayList<BluetoothDevice>();
    BluetoothAdapter mBluetoothAdapter;
    Button button;
    BluetoothGatt gatt;
    boolean unlock = false;
    double A = 0;

    public void update_beacon(String text)
    {
        final String set = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView)  findViewById(R.id.beacon);
                tv.setText("ID: " + set);
            }
        });
    }


    public void update_url(String text)
    {
        final String set = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView)  findViewById(R.id.url);
                tv.setText("URL: " + set);
            }
        });
    }

    public void update_voltage(String text)
    {
        final String set = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView)  findViewById(R.id.voltage);
                tv.setText("Voltage: " + set + " mV");
            }
        });
    }

    public void update_temperature(String text)
    {
        final String set = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView)  findViewById(R.id.temperature);
                tv.setText("Temperature: " + set + " C");
            }
        });
    }

    public void update_distance(String text)
    {
        final String set = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView)  findViewById(R.id.distance);
                tv.setText("Distance: " + set + " m");
            }
        });
    }


    int sw = 0;
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    Log.d("statechange", "Connection state changed");
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Log.d("statechange", "service discovered");
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic) {
                    Log.d("statechange", "Characteristic changed");
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.d("statechange", "Characteristic read");
                                    }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic,
                                                  int status) {
                    Log.d("statechange", "Characteristic write");
                }
            };


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {

                    if(device.getAddress().equals("FD:57:E0:DC:63:40"))
                    {
                        Log.d("onlescan","device: " + device.toString());
                        byte length = scanRecord[7];
                        byte frametype = scanRecord[11];

                        switch(frametype) {
                            //UID
                            case 0x00:
                            {
                                Byte txpower = scanRecord[12];
                                byte[] namespace = Arrays.copyOfRange(scanRecord, 13, 23);
                                byte[] instance = Arrays.copyOfRange(scanRecord, 23, 29);
                                Log.d("txpower", txpower.toString());
                                Log.d("namespace", namespace.toString());
                                Log.d("instance", instance.toString());

                                String sdistance = "unknown";
                                A = txpower;
                                if(true)
                                {
                                    Log.d("RSSI", rssi + "");
                                    Double distance = Math.pow(10, (rssi - (-74))/-20.0);
                                    sdistance = distance.toString();
                                }



                                String fullid = "";

                                for(byte b : namespace){
                                    fullid += String.format("%02X", b);
                                }

                                Log.d("fullid1", fullid);

                                fullid += " - ";

                                for(byte b : instance){
                                    fullid += String.format("%02X", b);
                                }
                                Log.d("fullid2", fullid);


                                update_distance(sdistance.toString());


                                update_beacon(fullid);
                                break;
                            }
                            //URL
                            case 0x10:
                            {
                                unlock = true;
                                Byte ranging = scanRecord[12];
                                Byte urlscheme = scanRecord[13];

                                String prefix = "";

                                A = ranging;

                                switch (urlscheme)
                                {
                                    case 0x00:{
                                        prefix = "http://www.";
                                        break;
                                    }
                                    case 0x01:{
                                        prefix = "https://www.";
                                        break;
                                    }
                                    case 0x02:{
                                        prefix = "http://";
                                        break;
                                    }
                                    case 0x03:{
                                        prefix = "https://";
                                        break;
                                    }
                                }
                                byte[] url = Arrays.copyOfRange(scanRecord, 14, 14 + length - 6);
                                Log.d("ranging", ranging.toString());
                                Log.d("urlscheme", urlscheme.toString());
                                Log.d("url", url.toString());

                                String surl = new String(url);
                                update_url(prefix + surl.toString());
                                break;
                            }

                            //TLM
                            case 0x20:
                            {

                                Byte tlmvers = scanRecord[12];
                                Byte bv = scanRecord[13];

                                byte[] battvolt = Arrays.copyOfRange(scanRecord, 13, 15);

                                byte[] beacontemp = Arrays.copyOfRange(scanRecord, 15, 17);
                                byte[] PDUcount = Arrays.copyOfRange(scanRecord, 17,21);
                                byte[] uptime = Arrays.copyOfRange(scanRecord, 21,25);

                                Log.d("bv", bv.toString());

                                Log.d("tlmvers", tlmvers.toString());
                                Log.d("battvolt", battvolt.toString());
                                Log.d("beacontemp", beacontemp.toString());
                                Log.d("beacontemp1", "" + beacontemp[0]);
                                Log.d("beacontemp2", "" + beacontemp[1]);

                                Log.d("volt1", String.format("0x%02X", battvolt[0]));
                                Log.d("volt2", String.format("0x%02X", battvolt[1]));

                                Log.d("PDUcount", PDUcount.toString());
                                Log.d("uptime", uptime.toString());



                                int volt = (((battvolt[0] << 8) &0xFFFF) | (battvolt[1] & 0xFF));
                                Integer ovolt = volt;
                                Float temperature = (float)beacontemp[0] + ((float)beacontemp[1]/256.0f);
                                Log.d("convert_temperature", temperature.toString());

                                update_temperature(temperature.toString());
                                update_voltage(ovolt.toString());

                                break;
                            }
                        }



                    }




                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("onCreate", "App Started");


        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("onClick", "Connect Button Clicked");
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("onLeScan", (devicelist.size()) + " devices found");
                                button.setEnabled(false);
                            }
                        });
                        // Initializes Bluetooth adapter.
                        bluetoothManager =
                                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                        mBluetoothAdapter = bluetoothManager.getAdapter();


                        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    button.setText("Bluetooth disabled");
                                    return;
                                }
                            });

                            Log.d("onClick", "Bluetooth disabled");

                            return;
                        }

                        mBluetoothAdapter.startLeScan( mLeScanCallback);

                        Log.d("onClick", "Trying to sleep");

                        try {

                            Thread.sleep((100000));
                        }
                        catch(Exception e)
                        {
                            Log.d("onClick", "Exception when sleeping");
                        }
                        Log.d("onClick", "Stopping scan");
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setEnabled(true);
                            }
                        });


                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStop()
    {
        super.onStop();
        if(gatt != null)
        {
            gatt.close();
        }
    }

}







