package comb.example.jumee.bledemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jumee on 2018-10-17.
 */

public class DevicesAdapter extends BaseAdapter {
    private List<BluetoothDevice> devices;
    private Context context;

    public DevicesAdapter(Context context, List<BluetoothDevice> devices) {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return devices == null ? null : devices.get(position);
    }

    @Override
    public int getCount() {
        return devices == null ? 0 : devices.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = getItem(position);

        View view = LayoutInflater.from(context).inflate(R.layout.devices_list_item, null);
        TextView tv_name = view.findViewById(R.id.tv_device_item);
        tv_name.setText(device.getName() + " \n" + device.getAddress());
        return view;
    }

    public void setDevicesList(List<BluetoothDevice> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

}
