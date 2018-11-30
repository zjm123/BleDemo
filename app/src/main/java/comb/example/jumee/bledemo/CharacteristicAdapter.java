package comb.example.jumee.bledemo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jumee on 2018-10-18.
 */

public class CharacteristicAdapter extends BaseAdapter {
    private Context context;
    private List<BluetoothGattCharacteristic> characteristicList;

    public CharacteristicAdapter(Context context, List<BluetoothGattCharacteristic> characteristicList) {
        this.context = context;
        this.characteristicList = characteristicList;

        if (this.characteristicList == null) {
            this.characteristicList = new ArrayList<>();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public BluetoothGattCharacteristic getItem(int position) {
        return characteristicList.get(position);
    }

    @Override
    public int getCount() {
        return characteristicList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothGattCharacteristic obj = getItem(position);

        View view = LayoutInflater.from(context).inflate(R.layout.characteristic_list_item, null);

        TextView tvItem = view.findViewById(R.id.tv_characteristic_item);
        tvItem.setText(obj.describeContents() + "\nUUID:" + obj.getUuid() + "\n" + "" + obj.getProperties());

        return view;
    }

}
