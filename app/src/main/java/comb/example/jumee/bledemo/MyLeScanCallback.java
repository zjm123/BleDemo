package comb.example.jumee.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Created by Jumee on 2018-10-17.
 */

public class MyLeScanCallback implements BluetoothAdapter.LeScanCallback {
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

    }
}
