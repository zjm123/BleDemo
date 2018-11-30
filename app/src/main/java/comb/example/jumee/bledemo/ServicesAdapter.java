package comb.example.jumee.bledemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
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

public class ServicesAdapter extends BaseAdapter {
    private List<BluetoothGattService> services;
    private Context context;

    public ServicesAdapter(Context context, List<BluetoothGattService> services) {
        this.context = context;
        this.services = services;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public BluetoothGattService getItem(int position) {
        return services == null ? null : services.get(position);
    }

    @Override
    public int getCount() {
        return services == null ? 0 : services.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothGattService service = getItem(position);

        View view = LayoutInflater.from(context).inflate(R.layout.services_list_item, null);
        TextView tv_name = view.findViewById(R.id.tv_service_item);
        tv_name.setText(service.getUuid() + "");
        return view;
    }

    public void setServicesList(List<BluetoothGattService> services) {
        this.services = services;
        notifyDataSetChanged();
    }

}
