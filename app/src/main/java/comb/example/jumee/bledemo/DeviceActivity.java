package comb.example.jumee.bledemo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceActivity extends AppCompatActivity {
    final static String TAG = "DeviceActivity";

    BluetoothDevice device;
    TextView tvConnect;
    TextView tvInfo;
    ListView listView;
    ServicesAdapter adapter;

    BluetoothGatt mBluetoothGatt;
    List<BluetoothGattService> services;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 1:
                    tvInfo.setText(tvInfo.getText() + "设备已连接.\n");
                    break;
                case 2:
                    tvInfo.setText(tvInfo.getText() + "设备连接失败.\n");
                    break;
                case 3:
                    tvInfo.setText(tvInfo.getText() + "设备不存在.\n");
                    break;
                case 100:
                    tvInfo.setText("发现服务成功.\n");
                    adapter.setServicesList(services);
                    break;
                case 101:
                    byte[] bytes = msg.getData().getByteArray("values");
                    String str = formatData(bytes);
                    tvInfo.setText(str);
                    break;
                case 102:
                    tvInfo.setText("onCharacteristicRead\n");
                    break;
                case 103:
                    tvInfo.setText("发现服务失败.\n");
                    break;
            }
            if (msg.what > 102) {
                tvInfo.setText(msg.what + "\n");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        Intent intent = getIntent();
        device = intent.getParcelableExtra("device");
        if (device != null) {
            setTitle(device.getName());
        }

        initView();
    }

    private void initView() {
        tvConnect = findViewById(R.id.tv_connect);
        tvInfo = findViewById(R.id.tv_info);
        listView = findViewById(R.id.lv_device);
        tvConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                adapter.setServicesList(null);
                tvInfo.setText("");
                connect();
            }
        });

        adapter = new ServicesAdapter(this, null);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @TargetApi(24)
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothGattService service = services.get(position);
//                Intent intent = new Intent(DeviceActivity.this, CharacteristicActivity.class);
//                intent.putExtra("service", service);
//                startActivityForResult(intent, 1122);

                if (service.getUuid().equals(BluetoothUtils.uuid_service_write)) {
                    final List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < gattCharacteristics.size(); i++) {
                                BluetoothGattCharacteristic characteristic = gattCharacteristics.get(i);
                                if (characteristic.getUuid().equals(BluetoothUtils.uuid_service_write_conf)) {
                                    characteristic.setValue(BluetoothUtils.getHexBytes("05"));
                                } else if (characteristic.getUuid().equals(BluetoothUtils.uuid_service_write_mode)) {
                                    characteristic.setValue(BluetoothUtils.getHexBytes("02"));
                                } else {
                                    return;
                                }
                                boolean bl = mBluetoothGatt.writeCharacteristic(characteristic);
                                if (!bl) {
                                    Message msg = new Message();
                                    msg.what = 103;
                                    handler.sendMessage(msg);
                                }
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                }
                            }
                        }
                    }).start();
                } else if (service.getUuid().equals(BluetoothUtils.uuid_service_read)) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();

                    for (int i = 0; i < gattCharacteristics.size(); i++) {
                        BluetoothUtils.enableIndications(mBluetoothGatt, gattCharacteristics.get(i));
                    }
                }
            }
        });
    }

    private boolean connect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        Message msg = new Message();
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        if (mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                Log.e(TAG, "Connect succeed.");
                msg.what = 1;
                handler.sendMessage(msg);
                return true;
            } else {
                Log.e(TAG, "Connect fail.");
                msg.what = 2;
                handler.sendMessage(msg);
                return false;
            }
        } else {
            Log.e(TAG, "BluetoothGatt null.");
            msg.what = 3;
            handler.sendMessage(msg);
            return false;
        }
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        // 当连接状态发生改变
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {//当蓝牙设备已经连接
                Log.e(TAG, "Connected to GATT server.");
                if (mBluetoothGatt.discoverServices()) {
                    Log.e(TAG, "Attempting to start service discovery: true");
                } else {
                    Log.e(TAG, "Attempting to start service discovery: false");
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//当设备无法连接
                Log.e(TAG, "Disconnected from GATT server.");
                if (device != null) {
                    Log.e(TAG, "重新连接");
                    connect();
                } else {
                    Log.e(TAG, "Disconnected from GATT server.");
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                services = mBluetoothGatt.getServices();
                Message msg = new Message();
                msg.what = 100;
                handler.sendMessage(msg);
            } else {
                Message msg = new Message();
                msg.what = 103;
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Message msg = new Message();
            msg.what = 101;
            msg.getData().putByteArray("values", characteristic.getValue());
            handler.sendMessage(msg);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBluetoothGatt.discoverServices();
        mBluetoothGatt.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1122) {
            if (resultCode == 1000) {
                BluetoothGattCharacteristic characteristic = data.getParcelableExtra("characteristic");
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);

                BluetoothUtils.enableIndications(mBluetoothGatt, characteristic);
            }
        }
    }

    private String formatData(byte[] bytes) {
        if (bytes.length != 19) {
            return "数据长度不正确.";
        } else if (!Arrays.equals(new byte[]{bytes[0]}, new byte[]{bytes[0]})) {
            return "数据前后不一致.";
        }

        String str = BluetoothUtils.bytesToHexString(bytes);
        StringBuilder sb = new StringBuilder();
        byte[] buff = new byte[]{bytes[0]};
        String color = BluetoothUtils.bytesToHexString(buff);
        if (color.equals("57")) {
            sb.append("白色");
        } else if (color.equals("42")) {
            sb.append("蓝色");
        } else if (color.equals("47")) {
            sb.append("绿色");
        } else {
            sb.append("未知颜色");
        }
        sb.append("\n");

//        int dd = BluetoothUtils.byteToInt(bytes[1]);
//        sb.append(dd);
//        sb.append("\n");
//
//        int direction = BluetoothUtils.byteToInt(bytes[2]) & 128;
//        sb.append(direction == 0 ? "S" : "N");
//        sb.append("(" + BluetoothUtils.byteArrToBinStr(new byte[]{bytes[2]}) + ")");
//        sb.append("\n");
//
//        int n = BluetoothUtils.byteToInt(bytes[2]) & 127;
//        String b3 = BluetoothUtils.intToHexStr(n);
//        sb.append("转换后->" + b3);
//        sb.append("\n");
//
//        String digital = b3 + BluetoothUtils.bytesToHexString(new byte[]{bytes[3], bytes[4]});
//        sb.append(digital);
//        sb.append("\n");
//
//        Integer dig = Integer.parseInt(digital, 16);
//        sb.append("得到的数字->" + dig);
//        sb.append("\n");
//
//        float val = dd + (dig / 1000000f);
//        sb.append("结果:" + String.valueOf(val));
//        sb.append("\n");

        Map<Integer, String> latitude = parseCoordinates(new byte[]{bytes[1], bytes[2], bytes[3], bytes[4]}, true);
        Map<Integer, String> longitude = parseCoordinates(new byte[]{bytes[5], bytes[6], bytes[7], bytes[8]}, false);
        sb.append(latitude.get(1) + "," + longitude.get(1));
        sb.append("\n");
        sb.append(latitude.get(2) + "," + longitude.get(2));
        sb.append("\n");

        float speed = parseSpeed(new byte[]{bytes[9], bytes[10], bytes[11]});
        sb.append("speed:" + speed);
        sb.append("\n");

        Date date = parseTime(new byte[]{bytes[12], bytes[13], bytes[14], bytes[15], bytes[16], bytes[17]});
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sb.append("date:" + format.format(date));
        sb.append("\n");

        return str + "\n" + sb.toString();
    }

    private Map<Integer, String> parseCoordinates(byte[] bytes, boolean isLatitude) {
        Map<Integer, String> map = new HashMap<>();

        int dd = BluetoothUtils.byteToInt(bytes[0]);
        int direction = BluetoothUtils.byteToInt(bytes[1]) & 128;
        if (isLatitude) {
            map.put(1, direction == 0 ? "S" : "N");
        } else {
            map.put(1, direction == 0 ? "W" : "E");
        }
        int n = BluetoothUtils.byteToInt(bytes[1]) & 127;
        String digital = BluetoothUtils.intToHexStr(n) + BluetoothUtils.bytesToHexString(new byte[]{bytes[2], bytes[3]});

        Integer dig = Integer.parseInt(digital, 16);
        float val = dd + (dig / 1000000f);

        map.put(2, String.valueOf(val));

        return map;
    }

    private float parseSpeed(byte[] bytes) {
        float speed;

        String str = BluetoothUtils.bytesToHexString(new byte[]{bytes[0], bytes[1]});
        Integer ddd = Integer.parseInt(str, 16);
        int ff = BluetoothUtils.byteToInt(bytes[2]);
        speed = ddd + (ff / 100f);

        return speed;
    }

    private Date parseTime(byte[] bytes) {
        int year = BluetoothUtils.byteToInt(bytes[5]);
        int month = BluetoothUtils.byteToInt(bytes[4]);
        int day = BluetoothUtils.byteToInt(bytes[3]);
        int hour = BluetoothUtils.byteToInt(bytes[0]);
        int minute = BluetoothUtils.byteToInt(bytes[1]);
        int second = BluetoothUtils.byteToInt(bytes[2]);
        Date date = new Date(year + 100, month, day, hour, minute, second);

        return date;
    }

}
