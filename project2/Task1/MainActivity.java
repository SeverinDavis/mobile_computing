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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothManager bluetoothManager;
    ArrayList<BluetoothDevice> devicelist = new ArrayList<BluetoothDevice>();
    BluetoothAdapter mBluetoothAdapter;
    Button button;
    BluetoothGatt gatt;
    TextView tempview;
    TextView humidview;
    List<BluetoothGattService> servicelist;
    Button temppoll;
    Button humidpoll;
    Button subscribe;
    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID WEATHER_UUID = UUID.fromString("00000002-0000-0000-fdfd-fdfdfdfdfdfd");
    private static final UUID TEMP_UUID = UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb");
    private static final UUID HUMID_UUID = UUID.fromString("00002a6f-0000-1000-8000-00805f9b34fb");
    int sw = 0;

    //declare bluetooth callbacks
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

            	//called when bluetooth connection changes
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

                    //logs all the services discovered on the device we connected to
                    for(int i  = 0; i< servicelist.size(); i++)
                        Log.d("Services", servicelist.get(i).getUuid().toString());

                    //There are three services on our device. so we use that as a sanity check
                    if(servicelist.size() == 3)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Service Found", Toast.LENGTH_SHORT).show();
                                //
                                // sendbutton.setEnabled(true);
                            }
                        });

                    }

                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic) {
                    Log.d("statechange", "Characteristic read");

                    //temperature and humidity can each cause a callback here. so we check if it's the temperature service or the humidity service
                    if(characteristic.getUuid().equals(TEMP_UUID))
                    {
                    	//update temperature UI
                    	//first byte in payload is not useful.
                    	//we know its float, so we extract float with offset 1, to ignore the first bye
                        final Float temperature = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
                        Log.d("statechange", temperature.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tempview.setText(temperature.toString());
                            }
                        });
                    }
                    //check if humidity service
                    if(characteristic.getUuid().equals(HUMID_UUID))
                    {
                    	//update humidity UI
                        final DecimalFormat df = new DecimalFormat("#.##");
                        df.setRoundingMode(RoundingMode.CEILING);

                        //extract Uint16 from payload and format into percentage with one decmial place
                        final Integer h = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                        final Double humid = h / 100.0;
                        Log.d("statechange", humid.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                humidview.setText(df.format(humid).toString() + "%");
                            }
                        });
                    }


                }

                @Override
                //CLONE OF CHARACTERISTIC CHANGED
                //we do the exact same thing. just check where our callback came from and update the UI accordingly
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.d("statechange", "Characteristic read");

                    if(characteristic.getUuid().equals(TEMP_UUID))
                    {
                        final Float temperature = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
                        Log.d("statechange", temperature.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tempview.setText(temperature.toString());
                            }
                        });
                    }
                    if(characteristic.getUuid().equals(HUMID_UUID))
                    {
                        final DecimalFormat df = new DecimalFormat("#.##");
                        df.setRoundingMode(RoundingMode.CEILING);

                        final Integer h = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                        final Double humid = h / 100.0;
                        Log.d("statechange", humid.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                humidview.setText(df.format(humid).toString() + "%");
                            }
                        });
                    }


                }

                @Override
                //more or less unused in this app
                // Result of a characteristic write operation
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic,
                                                  int status) {
                    Log.d("statechange", "Characteristic write");
                    Integer temp = status;
                    Log.d("statechange", temp.toString());

                }


            };


    //callback when a new device is discovered
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                	//we scan internally and only add the device to our list if the mac address matches
                    if(device.getAddress().equals("F6:B6:2A:79:7B:5D"))
                        devicelist.add(device);

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("onCreate", "App Started");
        tempview = (TextView) findViewById(R.id.textView3);
        temppoll = (Button) findViewById(R.id.button4);
        humidview = (TextView) findViewById(R.id.textView4);
        humidpoll = (Button) findViewById(R.id.button3);
        subscribe = (Button) findViewById(R.id.button6);



        subscribe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//subscribe/unsubscribe button
                if(subscribe.getText().equals("Subscribe")) {
                    subscribe.setText("Unsubscribe");
                    if (servicelist == null)
                        return;
                    //we try to find the weather UUID
                    int service_found = -1;
                    UUID uuid = WEATHER_UUID;
                    for (int i = 0; i < servicelist.size(); i++) {
                        Log.d("services", servicelist.get(i).getUuid().toString());
                        if (servicelist.get(i).getUuid().toString().equals(uuid.toString())) {
                            service_found = i;
                            Log.d("services", "service found");

                        }
                    }
                    if (service_found < 0)
                        return;

                    Log.d("readtemp", "service found");

                    //if we found the weather service, we can subscribe to it.
                    //we pull the characteristic at index 0 first. 
                    //this is the temperature characteristic and subscribe to it.

                    BluetoothGattCharacteristic tgc = (servicelist.get(service_found).getCharacteristics()).get(sw);
                    gatt.setCharacteristicNotification(tgc, true);
                    BluetoothGattDescriptor tdesc = tgc.getDescriptor(CONFIG_DESCRIPTOR);
                    tdesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(tdesc);


                    //if we try to subscribe to the humidity updates right away, it doesn't work. so we wait a little bit
                    try
                    {
                        Thread.sleep(300);
                    }
                    catch(Exception e) {
                        return;

                    }

                    //we pull the second characteristic at index 1. this is humidity and subscribe to it.
                    sw++;
                     tgc = (servicelist.get(service_found).getCharacteristics()).get(sw);
                    gatt.setCharacteristicNotification(tgc, true);
                     tdesc = tgc.getDescriptor(CONFIG_DESCRIPTOR);
                    tdesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(tdesc);

                    sw = 0;



                }

                //CLONE OF SUBSCRIPTION
                //this is literally the same thing except we unsubscribe from both services
                else {
                    subscribe.setText("Subscribe");
                    if (servicelist == null)
                        return;
                    int service_found = -1;
                    UUID uuid = WEATHER_UUID;
                    for (int i = 0; i < servicelist.size(); i++) {
                        Log.d("services", servicelist.get(i).getUuid().toString());
                        if (servicelist.get(i).getUuid().toString().equals(uuid.toString())) {
                            service_found = i;
                            Log.d("services", "service found");

                        }
                    }
                    if (service_found < 0)
                        return;

                    Log.d("readtemp", "service found");

                    BluetoothGattCharacteristic tgc = (servicelist.get(service_found).getCharacteristics()).get(sw);
                    gatt.setCharacteristicNotification(tgc, false);
                    BluetoothGattDescriptor tdesc = tgc.getDescriptor(CONFIG_DESCRIPTOR);
                    tdesc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(tdesc);

                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        return;

                    }


                    sw++;
                    tgc = (servicelist.get(service_found).getCharacteristics()).get(sw);
                    gatt.setCharacteristicNotification(tgc, false);
                    tdesc = tgc.getDescriptor(CONFIG_DESCRIPTOR);
                    tdesc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(tdesc);

                    sw = 0;
                }
            }
        });

		//this reads the humidity characteristic
        humidpoll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//check if we have our service
                if(servicelist == null)
                    return;
                int service_found = -1;
                UUID uuid = WEATHER_UUID;
                for(int i = 0; i< servicelist.size(); i++)
                {
                    Log.d("services",servicelist.get(i).getUuid().toString());
                    if(servicelist.get(i).getUuid().toString().equals(uuid.toString())) {
                        service_found = i;
                        Log.d("services","service found");

                    }
                }
                if(service_found < 0)
                    return;

                Log.d("readtemp", "service found");

                //retrieve humidity characterisitic and send it back with readCharacteristic()
                BluetoothGattCharacteristic bgc = (servicelist.get(service_found).getCharacteristics()).get(1);
                Log.d("readtemp", "trying to read");
                gatt.readCharacteristic(bgc);
            }
        });


        //CLONE OF HUMIDITY READ FUNCTION
        //does the exact same thing as the humidity function. but it reads temperature
        temppoll.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                  if(servicelist == null)
                      return;
                  int service_found = -1;
                  UUID uuid = WEATHER_UUID;
                  for(int i = 0; i< servicelist.size(); i++)
                  {
                      Log.d("services",servicelist.get(i).getUuid().toString());
                      if(servicelist.get(i).getUuid().toString().equals(uuid.toString())) {
                          service_found = i;
                          Log.d("services","service found");

                      }
                  }
                  if(service_found < 0)
                      return;

                  Log.d("readtemp", "service found");

                  BluetoothGattCharacteristic bgc = (servicelist.get(service_found).getCharacteristics()).get(0);
                  Log.d("readtemp", "trying to read");
                  gatt.readCharacteristic(bgc);
              }
        });

        //this scans for bluetooth devices and then automatically connects
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

                        //start bluetooth scan
                        mBluetoothAdapter.startLeScan( mLeScanCallback);

                        Log.d("onClick", "Trying to sleep");

                        //wait for devices to be discovered
                        try {

                            Thread.sleep((2000));
                        }
                        catch(Exception e)
                        {
                            Log.d("onClick", "Exception when sleeping");
                        }
                        Log.d("onClick", "Stopping scan");
                        //stop bluetooth scan
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), devicelist.size() + " devices found", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //check if we found our device in the scan
                        if(devicelist.size() == 0)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    button.setEnabled(true);
                                    Toast.makeText(getApplicationContext(), "0 Devices found", Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                        else
                        {
                        	//connect to device and register callback
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
                                }
                            });

                        }

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







