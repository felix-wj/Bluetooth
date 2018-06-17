package com.example.bluetooth;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.CountDownTimer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONStringer;

public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }


    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private boolean OpenBluetoothFlag = false;
    private IntentFilter localIntentFilter;

    private ArrayList<Device> pairedListData, newDevicesListData;
    private ListView pairedListView, newDevicesListView;
    private DevicesAdapter pairedDevicesAdapter, newDevicesDevicesAdapter;

    private float degree = 0;

    //	private float[][] distanceData;
    //工具
    private CountDownTimer timer;
    private Handler handler;
    private boolean flag = false;


    private SensorManager sensorManager;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

      /*  StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());*/

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pairedListData = new ArrayList<>();
        newDevicesListData = new ArrayList<>();
        pairedListView = (ListView) findViewById(R.id.paired_devices);
        newDevicesListView = (ListView) findViewById(R.id.new_devices);
        pairedDevicesAdapter = new DevicesAdapter(this, R.layout.device_information, pairedListData);
        newDevicesDevicesAdapter = new DevicesAdapter(this, R.layout.device_information, newDevicesListData);
        pairedListView.setAdapter(pairedDevicesAdapter);
        newDevicesListView.setAdapter(newDevicesDevicesAdapter);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);


        // Get the local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
        // 判断是否打开蓝牙
        if (!bluetoothAdapter.isEnabled()) {
            // 弹出对话框提示用户是后打开
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
            // 不做提示，强行打开
            // mBluetoothAdapter.enable();
        } else {
            // 不做提示，强行打开
            bluetoothAdapter.enable();
        }
        //扫描蓝牙设备
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "蓝牙设备不可用！", Toast.LENGTH_SHORT).show();
            finish();
        } else if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }


        //持续监听搜索蓝牙
        timer = new CountDownTimer(24 * 60 * 60 * 1000, 1000) {


            public void updateLocation(double angle, double distance) {
                String msg = "";
                try { //创建一个HttpClient对象
                    HttpClient httpclient = new DefaultHttpClient();
                    URL url = new URL("http://111.231.137.44:8080/receive?distance=" + distance + "&angle=" + angle);
                    Log.d("远程URL", String.valueOf(url));
                    //创建HttpGet对象
                    HttpGet request = new HttpGet(String.valueOf(url));
                    request.addHeader("Accept", "text/json");
                    HttpResponse response = httpclient.execute(request);
                    //获取HttpEntity
                    HttpEntity entity = response.getEntity();
                    //获取响应的结果信息
                    String json = EntityUtils.toString(entity, "UTF-8");

                    if (json != null) {
                        Log.d("结果", String.valueOf(json));
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    Log.d("结果", e.getMessage());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.d("结果", e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("结果", e.getMessage());
                }
            }

            @Override
            public void onTick(long millisUntilFinished) {
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();
                localIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//("android.bluetooth.device.action.FOUND");
                registerReceiver(new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        BluetoothDevice localBluetoothDevice = null;

                        //未绑定设备
                        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                            localBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//("android.bluetooth.device.extra.DEVICE");
                            if (localBluetoothDevice != null && localBluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {//未绑定设备
                                 Device tempDevice = new Device(localBluetoothDevice.getName(), localBluetoothDevice.getAddress(), intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI));

                             // Log.v("??????degeree",""+degree);
                                tempDevice.setDegree(degree);

                                flag = false;
                                for (int i = 0; i < newDevicesListData.size(); i++) {
                                    Device device = newDevicesListData.get(i);
                                    if (device.getMac().equals(tempDevice.getMac())) {
                                        newDevicesListData.set(i, tempDevice);
                                        newDevicesDevicesAdapter.notifyDataSetChanged();




                                        flag = true;
                                        break;
                                    }
                                }
                                if (!flag) {
                                    newDevicesListData.add(tempDevice);
                                    newDevicesDevicesAdapter.notifyDataSetChanged();
                                }


                                if ( tempDevice!= null &&"小米手机".equals(tempDevice.getName())){
                                    Log.v("蓝牙名称",tempDevice.getName());
                                    Log.v("蓝牙Mac",tempDevice.getMac());
                                    Log.v("蓝牙距离",tempDevice.getDistance()+"");
                                    Log.v("角度",tempDevice.getDegree()+"");

                                    final float degree = tempDevice.getDegree();
                                    final float distance = tempDevice.getDistance();
                                    ThreadPoolUtil.getExecutorService().execute(new Runnable() {
                                        public void run() {
                                            updateLocation(degree,distance);
                                        }
                                    });
                                   /* Thread thread = new Thread() {
                                        @Override
                                        public void run() {
                                            updateLocation(degree,distance);
                                        }
                                    };
                                    thread.start();*/
                                }

                            }
                        }
                        //已绑定设备 == 12
                        do {
                            do {
                                if (localBluetoothDevice != null) {
                                    Device tempDevice2 = new Device(localBluetoothDevice.getName(), localBluetoothDevice.getAddress(), intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI));
                                    flag = false;
                                    for (int i = 0; i < pairedListData.size(); i++) {
                                        Device device = pairedListData.get(i);
                                        if (device.getMac().equals(tempDevice2.getMac())) {
                                            pairedListData.set(i, tempDevice2);
                                            pairedDevicesAdapter.notifyDataSetChanged();
                                            flag = true;
                                            break;
                                        }
                                    }
                                    if (!flag) {
                                        pairedListData.add(tempDevice2);
                                        pairedDevicesAdapter.notifyDataSetChanged();
                                    }
                                    if ( tempDevice2!= null &&"小米手机".equals(tempDevice2.getName())){
                                        Log.v("蓝牙名称",tempDevice2.getName());
                                        Log.v("蓝牙Mac",tempDevice2.getMac());
                                        Log.v("蓝牙距离",tempDevice2.getDistance()+"");
                                        Log.v("角度",tempDevice2.getDegree()+"");

                                        final float degree = tempDevice2.getDegree();
                                        final float distance = tempDevice2.getDistance();
                                        ThreadPoolUtil.getExecutorService().execute(new Runnable() {
                                            public void run() {
                                                updateLocation(degree,distance);
                                            }
                                        });
                                        /*Thread thread = new Thread() {
                                            @Override
                                            public void run() {
                                                updateLocation(degree,distance);
                                            }
                                        };
                                        thread.start();*/
                                    }
                                }
                                return;
                            } while (!BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action));
                        } while (newDevicesListData.size() != 0);
                    }
                }, localIntentFilter);
            }

            @Override
            public void onFinish() {

            }
        }.start();

        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        break;
                    case 1:
                        pairedDevicesAdapter.notifyDataSetChanged();
                        break;
                    case 2:
                        newDevicesDevicesAdapter.notifyDataSetChanged();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private SensorEventListener listener = new SensorEventListener() {
        float[] accelerometerValues = new float[3];
        float[] magneticValues = new float[3];

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values.clone();
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticValues = event.values.clone();
            }
            float[] R = new float[9];
            float[] values = new float[3];
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
            SensorManager.getOrientation(R, values);

            degree = -(float) Math.toDegrees(values[0]);//旋转角度
           // Device.setDegree(degree);
            //Log.v("-----------度", String.valueOf(degree));
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

}
