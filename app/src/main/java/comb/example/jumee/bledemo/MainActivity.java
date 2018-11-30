package comb.example.jumee.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "MainActivity";
    TextView tvStart;
    ListView lvDevice;
    DevicesAdapter adapter;
    List<BluetoothDevice> devices;

    // 10秒后停止寻找.
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    Toast.makeText(MainActivity.this, "mBluetoothAdapter is null.", Toast.LENGTH_LONG).show();
                    break;
                case 20:
                    adapter.setDevicesList(devices);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devices = new ArrayList<>();
        initView();

        requestPermission();
    }

    private void initView() {
        tvStart = findViewById(R.id.tv_start);
        lvDevice = findViewById(R.id.lv_services);

        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScanning = true;
                scanLeDevice(mScanning);
            }
        });

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        adapter = new DevicesAdapter(this, devices);
        lvDevice.setAdapter(adapter);
        lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                intent.putExtra("device", devices.get(position));
                startActivity(intent);
            }
        });
    }

    private void scanLeDevice(final boolean enable) {
        if (mBluetoothAdapter == null) {
            Message msg = new Message();
            msg.what = 10;
            mHandler.sendMessage(msg);
            return;
        }
        if (enable) {
            // 经过预定扫描期后停止扫描
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setScan(false);
                }
            }, SCAN_PERIOD);

            devices.clear();
            setScan(true);
        } else {
            setScan(false);
        }
    }

    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if ("TripLog Drive".equals(device.getName())) {
                //判断是否已存在
                boolean canadd = true;
                for (BluetoothDevice temp : devices) {
                    if (temp.getAddress().equals(device.getAddress())) {
                        canadd = false;
                    }
                }
                if (canadd) {
                    Log.e(TAG, device.getName() + "," + device.getAddress());
                    devices.add(device);

                    Message msg = new Message();
                    msg.what = 20;
                    mHandler.sendMessage(msg);
                }
            }
        }
    };

    private void setScan(boolean isScan) {
        mScanning = isScan;
        if (isScan) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private void requestPermission() {
        PermisionUtils.verifyStoragePermissions(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermisionUtils.REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Allowed
            } else {
                // Permission Denied.
            }
        }
    }
}
