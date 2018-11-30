package comb.example.jumee.bledemo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class CharacteristicActivity extends AppCompatActivity {
    List<BluetoothGattCharacteristic> gattCharacteristics;
    BluetoothGattService service;
    TextView tvInfo;
    ListView lvCharacteristic;

    CharacteristicAdapter adapter;

    BluetoothGattCharacteristic characteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_characteristic);

        service = getIntent().getParcelableExtra("service");

        initView();
    }

    private void initView() {
        tvInfo = findViewById(R.id.tv_info);
        lvCharacteristic = findViewById(R.id.lv_characteristic);

        gattCharacteristics = service.getCharacteristics();
        adapter = new CharacteristicAdapter(this, gattCharacteristics);
        lvCharacteristic.setAdapter(adapter);

        lvCharacteristic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @TargetApi(24)
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                characteristic = gattCharacteristics.get(position);
                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                String str = "";
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    str += descriptor.toString() + ":" + descriptor.getUuid() + "\n";
                }
                tvInfo.setText(str);

                Intent intent = new Intent();
                intent.putExtra("characteristic", characteristic);
                setResult(1000, intent);
                finish();
            }
        });


    }

}
