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
                if(subscribe.getText().equals("Subscribe")) {
                    subscribe.setText("Unsubscribe");
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
                    gatt.setCharacteristicNotification(tgc, true);
                    BluetoothGattDescriptor tdesc = tgc.getDescriptor(CONFIG_DESCRIPTOR);
                    tdesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(tdesc);

                    try
                    {
                        Thread.sleep(300);
                    }
                    catch(Exception e) {
                        return;

                    }


                    sw++;
                     tgc = (servicelist.get(service_found).getCharacteristics()).get(sw);
                    gatt.setCharacteristicNotification(tgc, true);
                     tdesc = tgc.getDescriptor(CONFIG_DESCRIPTOR);
                    tdesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(tdesc);

                    sw = 0;



                }
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

        humidpoll.setOnClickListener(new View.OnClickListener() {
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

                BluetoothGattCharacteristic bgc = (servicelist.get(service_found).getCharacteristics()).get(1);
                Log.d("readtemp", "trying to read");
                gatt.readCharacteristic(bgc);
            }
        });



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

                            Thread.sleep((2000));
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







