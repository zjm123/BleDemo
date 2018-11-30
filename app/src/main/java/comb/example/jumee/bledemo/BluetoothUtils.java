package comb.example.jumee.bledemo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

/**
 * Created by Jumee on 2018-10-19.
 */

public class BluetoothUtils {
    final static UUID uuid_service_write = UUID.fromString("52fd43c4-8dd2-4706-8f5e-0c360d509b3c");
    final static UUID uuid_service_read = UUID.fromString("0c0a82f1-b7dd-44de-baf0-a3bb46ef239f");

    final static UUID uuid_service_write_conf = UUID.fromString("5174ebd1-41b2-4f95-bb7d-32922beec405");
    final static UUID uuid_service_write_mode = UUID.fromString("bd7ff55f-e162-4d2b-8036-c359b9162a2c");

    /**
     * 是否开启蓝牙的通知
     *
     * @param enable
     * @param characteristic
     * @return
     */
    public static boolean enableNotification(BluetoothGatt bluetoothGatt, boolean enable, BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null || characteristic == null) {
            return false;
        }
        if (!bluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
            return false;
        }
        //获取到Notify当中的Descriptor通道  然后再进行注册
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString(UUIDManager.NOTIFY_DESCRIPTOR));
        if (clientConfig == null) {
            return false;
        }
        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return bluetoothGatt.writeDescriptor(clientConfig);
    }

    public static boolean enableIndications(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0)
            return false;

        boolean bl = bluetoothGatt.setCharacteristicNotification(characteristic, true);
        if (bl) {
            for (BluetoothGattDescriptor dp : characteristic.getDescriptors()) {
                if (dp != null && bl) {
                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                        dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    }
                    bl = bluetoothGatt.writeDescriptor(dp);
                }
            }
        }
        return bl;
    }

    /**
     * 将字节 转换为16进制字符串
     *
     * @param src 需要转换的字节数组
     * @return 返回转换完之后的数据
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 将字符串转化为16进制的字节
     *
     * @param message 需要被转换的字符
     * @return
     */
    public static byte[] getHexBytes(String message) {
        int len = message.length() / 2;
        char[] chars = message.toCharArray();

        String[] hexStr = new String[len];

        byte[] bytes = new byte[len];

        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
    }

    /*
     * 字节转10进制
     */
    public static int byteToInt(byte b) {
        int r = (int) b;
        return r;
    }

    /**
     * byte数组转换为二进制字符串
     **/
    public static String byteArrToBinStr(byte[] b) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            result.append(Long.toString(b[i] & 0xff, 2));
        }
        return result.toString();
    }

    /**
     * int转16进制字符串
     *
     * @param b
     * @return
     */
    public static String intToHexStr(int b) {
        return String.format("%02x", b);
    }

}
