package com.example.sever.myapplication2;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.widget.Toast;

import com.example.sever.myapplication2.R;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothManager bluetoothManager;
    ArrayList<BluetoothDevice> devicelist = new ArrayList<BluetoothDevice>();
    BluetoothAdapter mBluetoothAdapter;
    Button button;
    Button sendbutton;
    BluetoothGatt gatt;
    EditText et;
    List<BluetoothGattService> servicelist;


    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    Log.d("statechange", "Connection state cahnged");
                    gatt.discoverServices();
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Log.d("statechange", "service discovered");
                    gatt.getServices();
                    servicelist = gatt.getServices();
                    Log.d("services", "list size " + servicelist.size());

                    for(int i  = 0; i< servicelist.size(); i++)
                        Log.d("Services", servicelist.get(i).getUuid().toString());

                    if(servicelist.size() == 3)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Service Found", Toast.LENGTH_SHORT).show();
                                sendbutton.setEnabled(true);
                            }
                        });

                    }

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
                    Integer temp = status;
                    Log.d("statechange", temp.toString());

                }


            };


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    if(device.getAddress().equals("F8:20:74:F7:2B:82"))
                        devicelist.add(device);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.getAddress());
                        }
                    });
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("onCreate", "App Started");

        et = (EditText) findViewById(R.id.editText2);
        sendbutton = (Button) findViewById(R.id.button2);
        sendbutton.setEnabled(false);
        sendbutton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {

                 Log.d("sendbutton", "sendbutton clicked");
                 String message = et.getText().toString();
                 Integer intmessage = 0;
                 if(message.equals(""))
                 {
                     intmessage = 0;
                 }

                 else
                 {
                     intmessage = Integer.parseInt((message));
                 }


                 if(intmessage > 65535)
                     intmessage = 65535;
                 if(intmessage < 0)
                     intmessage = 0;
                 Log.d("services", "list size " + servicelist.size());
                 if(servicelist == null)
                     return;
                 int service_found = -1;
                 UUID uuid = UUID.fromString("00000001-0000-0000-fdfd-fdfdfdfdfdfd");
                 for(int i = 0; i< servicelist.size(); i++)
                 {
                     Log.d("services",servicelist.get(i).getUuid().toString());
                     if(servicelist.get(i).getUuid().toString().equals(uuid.toString())) {
                         service_found = i;
                         Log.d("services","service found");

                     }
                 }

                 if(service_found < 0)
                 {
                     return;
                 }
                 Log.d("send", "service found");


                 BluetoothGattCharacteristic bgc = (servicelist.get(service_found).getCharacteristics()).get(0);
                 Log.d("send", intmessage.toString());


                 if(bgc.setValue(intmessage.intValue() & 0xFFFF, 0x12, 0)) {
                     Log.d("send", "set characteristic");
                     gatt.writeCharacteristic(bgc);

                 }

             }
        });

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
                                button.setClickable(false);
                            }
                        });
                        // Initializes Bluetooth adapter.
                        bluetoothManager =
                                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                        mBluetoothAdapter = bluetoothManager.getAdapter();


                        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                            button.setText("Bluetooth disabled");
                            Log.d("onClick", "Bluetooth disabled");

                            return;
                        }

                        mBluetoothAdapter.startLeScan( mLeScanCallback);

                        Log.d("onClick", "Trying to sleep");

                        try {

                            Thread.sleep((5000));
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
                                Toast.makeText(getApplicationContext(), devicelist.size() + " devices found", Toast.LENGTH_SHORT).show();
                            }
                        });

                        gatt = devicelist.get(0).connectGatt(MainActivity.super.getApplicationContext(), false, mGattCallback);
                        Log.d("services", "connected");
                        List<BluetoothGattService> servicelist = gatt.getServices();
                        Log.d("services", "list size " + servicelist.size());

                        for(int i  = 0; i< servicelist.size(); i++)
                            Log.d("Services", servicelist.get(i).toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setEnabled(true);
                                button.setClickable(true);
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






