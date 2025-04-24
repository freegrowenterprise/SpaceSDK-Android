package com.growspace.sdk.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.growspace.sdk.bluetooth.model.BluetoothLERemoteDevice;
import com.growspace.sdk.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BluetoothLEManagerHelper {
    private static final String TAG = "BluetoothLEManagerHelper";
    private final BroadcastReceiver bluetoothStateChangeReceiver;
    private final BroadcastReceiver bondStateChangeReceiver;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothManager mBluetoothManager;
    private Context mContext;
    private static UUID serviceUUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private static UUID rxCharacteristicUUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    private static UUID txCharacteristicUUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    private static UUID descriptorUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private Listener mListener = null;
    private HashMap<String, BluetoothLERemoteDevice> mBluetoothLERemoteDeviceList = new HashMap<>();
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int i, ScanResult scanResult) {
            if (ActivityCompat.checkSelfPermission(BluetoothLEManagerHelper.this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
                BluetoothLEManagerHelper.this.onScan(scanResult.getDevice().getName(), scanResult.getDevice().getAddress());
            } else {
                Log.e(BluetoothLEManagerHelper.TAG, "Missing required permission to get scanned device info");
            }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onConnectionStateChange. Status: " + i + " State: " + i2);
            if (i != 0) {
                BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
                BluetoothLEManagerHelper.this.onDisconnect(bluetoothGatt.getDevice().getAddress());
                return;
            }
            if (i2 != 2) {
                if (i2 == 0) {
                    BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
                    BluetoothLEManagerHelper.this.onDisconnect(bluetoothGatt.getDevice().getAddress());
                    return;
                }
                return;
            }
            BluetoothLERemoteDevice bluetoothLERemoteDevice = new BluetoothLERemoteDevice();
            bluetoothLERemoteDevice.setBluetoothGatt(bluetoothGatt);
            BluetoothLEManagerHelper.this.mBluetoothLERemoteDeviceList.put(bluetoothGatt.getDevice().getAddress(), bluetoothLERemoteDevice);
            if (BluetoothLEManagerHelper.this.discoverServices(bluetoothGatt)) {
                return;
            }
            Log.e(BluetoothLEManagerHelper.TAG, "Failed to start discover services");
            BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onServicesDiscovered status: " + i);
            if (!BluetoothLEManagerHelper.this.getCharacteristics(bluetoothGatt)) {
                Log.e(BluetoothLEManagerHelper.TAG, "Failed to start get characteristics");
                BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
            }
            if (BluetoothLEManagerHelper.this.writeDescriptorEnableNotification(bluetoothGatt)) {
                return;
            }
            Log.e(BluetoothLEManagerHelper.TAG, "Failed to start write descriptor to enable notification");
            BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            Log.d(BluetoothLEManagerHelper.TAG, "onCharacteristicChanged");
            if (BluetoothLEManagerHelper.this.readCharacteristicData(bluetoothGatt, bluetoothGattCharacteristic)) {
                return;
            }
            Log.e(BluetoothLEManagerHelper.TAG, "Failed to start read characteristic data");
            BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            Log.d(BluetoothLEManagerHelper.TAG, "onDescriptorWrite status: " + i);
            if (i == 0) {
                if (BluetoothLEManagerHelper.this.updateMtu(bluetoothGatt)) {
                    return;
                }
                Log.e(BluetoothLEManagerHelper.TAG, "Failed to start update MTU");
                BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
                return;
            }
            Log.e(BluetoothLEManagerHelper.TAG, "Failed to write descriptor");
            BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onCharacteristicWrite. Status: " + i);
            if (i != 0) {
                Log.e(BluetoothLEManagerHelper.TAG, "Failed to write characteristic");
                BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onCharacteristicRead. Status: " + i);
            if (i != 0) {
                Log.e(BluetoothLEManagerHelper.TAG, "Failed to read characteristic");
                BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onCharacteristicRead. Status: " + i);
            if (i != 0) {
                Log.e(BluetoothLEManagerHelper.TAG, "Failed to read descriptor");
                BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt bluetoothGatt, int i, int i2) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onMtuChanged. Status: " + i2);
            if (i2 == 0) {
                if (ActivityCompat.checkSelfPermission(BluetoothLEManagerHelper.this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
                    BluetoothLEManagerHelper.this.onConnect(bluetoothGatt.getDevice().getName(), bluetoothGatt.getDevice().getAddress());
                    return;
                } else {
                    Log.e(BluetoothLEManagerHelper.TAG, "Missing required permission to get scanned device info");
                    return;
                }
            }
            Log.e(BluetoothLEManagerHelper.TAG, "Failed to update MTU");
            BluetoothLEManagerHelper.this.close(bluetoothGatt.getDevice().getAddress());
        }
    };

    public interface Listener {
        void onBluetoothLEDataReceived(String str, byte[] bArr);

        void onBluetoothLEDeviceBonded(String str, String str2);

        void onBluetoothLEDeviceConnected(String str, String str2);

        void onBluetoothLEDeviceDisconnected(String str);

        void onBluetoothLEDeviceScanned(String str, String str2);

        void onBluetoothLEStateChanged(int i);
    }

    public BluetoothLEManagerHelper(Context context) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context2, Intent intent) {
                if (ActivityCompat.checkSelfPermission(BluetoothLEManagerHelper.this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
                    String action = intent.getAction();
                    if (action == null || !action.equals("android.bluetooth.device.action.BOND_STATE_CHANGED")) {
                        return;
                    }
                    int intExtra = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1);
                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");

                    if (bluetoothDevice != null) {
                        Log.d(BluetoothLEManagerHelper.TAG, "Bluetooth bond state changed to " + intExtra + " for device " + bluetoothDevice.getAddress());
                    } else {
                        Log.e(BluetoothLEManagerHelper.TAG, "Bluetooth bond state changed to " + intExtra + " for device bluetoothDevice Null");
                        return;
                    }

                    if (intExtra != 12) {
                        return;
                    }

                    BluetoothLEManagerHelper.this.onBonded(bluetoothDevice.getName(), bluetoothDevice.getAddress());
                } else {
                    Log.e(BluetoothLEManagerHelper.TAG, "Missing required permission to get bonded device info");
                }
            }
        };
        this.bondStateChangeReceiver = broadcastReceiver;
        this.bluetoothStateChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context2, Intent intent) {
                if (Objects.equals(intent.getAction(), "android.bluetooth.adapter.action.STATE_CHANGED")) {
                    int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                    Log.d(BluetoothLEManagerHelper.TAG, "Bluetooth state changed to " + intExtra);
                    BluetoothLEManagerHelper.this.onStateChanged(intExtra);
                }
            }
        };
        this.mContext = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothManager = bluetoothManager;
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        this.mBluetoothAdapter = adapter;
        this.mBluetoothLeScanner = adapter.getBluetoothLeScanner();
        this.mContext.registerReceiver(broadcastReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
    }

    public void registerListener(Listener listener) {
        this.mListener = listener;
        this.mContext.registerReceiver(this.bluetoothStateChangeReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
    }

    public void unregisterListener() {
        this.mListener = null;
        this.mContext.unregisterReceiver(this.bluetoothStateChangeReceiver);
    }

    public boolean isSupported() {
        return this.mBluetoothAdapter != null;
    }

    public boolean isEnabled() {
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public boolean isBondedDevice(String str) {
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            Iterator<BluetoothDevice> it = this.mBluetoothAdapter.getBondedDevices().iterator();
            while (it.hasNext()) {
                if (str.equals(it.next().getAddress())) {
                    return true;
                }
            }
            return false;
        }
        Log.e(TAG, "Missing required permission to scan for BLE devices");
        return false;
    }

    public boolean startLeDeviceScan() {
        String str = TAG;
        Log.d(str, "Bluetooth starting LE Scanning");
        if (this.mBluetoothLeScanner == null) {
            Log.e(str, "BluetoothLeScanner not initialized");
            return false;
        }
        ArrayList<ScanFilter> arrayList = new ArrayList<>();
        arrayList.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(serviceUUID)).build());
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(1);
        builder.setReportDelay(0L);
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_SCAN") == 0) {
            Log.d(str, "Bluetooth SCAN successfully started");
            this.mBluetoothLeScanner.startScan(arrayList, builder.build(), this.scanCallback);
            return true;
        }
        Log.e(str, "Missing required permission to scan for BLE devices");
        return false;
    }

    public boolean stopLeDeviceScan() {
        String str = TAG;
        Log.d(str, "Bluetooth stopping LE Scanning");
        if (this.mBluetoothLeScanner == null) {
            Log.e(str, "BluetoothLeScanner not initialized");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_SCAN") == 0) {
            Log.d(str, "Bluetooth SCAN successfully stopped");
            this.mBluetoothLeScanner.flushPendingScanResults(this.scanCallback);
            this.mBluetoothLeScanner.stopScan(this.scanCallback);
            return true;
        }
        Log.e(str, "Missing required permission to scan for BLE devices");
        return false;
    }

    public boolean connect(String str) {
        String str2 = TAG;
        Log.d(str2, "Proceed to connect to device: " + str);
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter == null || str == null) {
            Log.e(str2, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(str);
        if (remoteDevice == null) {
            Log.e(str2, "Device not found. Unable to connect.");
            return false;
        }
        close(str);
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            remoteDevice.connectGatt(this.mContext, false, this.mGattCallback);
            return true;
        }
        Log.e(str2, "Missing required permission to connect to device");
        return false;
    }

    public boolean transmit(String str, byte[] bArr) {
        String str2 = TAG;
        Log.d(str2, "Proceed to transmit to " + str + " data: " + Utils.byteArrayToHexString(bArr));
        BluetoothLERemoteDevice bluetoothLERemoteDevice = this.mBluetoothLERemoteDeviceList.get(str);
        if (bluetoothLERemoteDevice == null || bluetoothLERemoteDevice.getBluetoothGatt() == null || bluetoothLERemoteDevice.getRxCharacteristic() == null) {
            Log.e(str2, "BluetoothGatt not initialized or uninitialized characteristic for this device.");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            bluetoothLERemoteDevice.getRxCharacteristic().setValue(bArr);
            bluetoothLERemoteDevice.getRxCharacteristic().setWriteType(2);
            return bluetoothLERemoteDevice.getBluetoothGatt().writeCharacteristic(bluetoothLERemoteDevice.getRxCharacteristic());
        }
        Log.e(str2, "Missing required permission to write characteristic");
        return false;
    }

    public boolean close(String str) {
        String str2 = TAG;
        Log.d(str2, "Proceed to close connection with device " + str);
        BluetoothLERemoteDevice bluetoothLERemoteDevice = this.mBluetoothLERemoteDeviceList.get(str);
        if (bluetoothLERemoteDevice == null || bluetoothLERemoteDevice.getBluetoothGatt() == null) {
            Log.e(str2, "BluetoothGatt not initialized or uninitialized characteristic for this device.");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            bluetoothLERemoteDevice.getBluetoothGatt().close();
            this.mBluetoothLERemoteDeviceList.remove(str);
            return true;
        }
        Log.e(str2, "Missing required permission to close connection");
        return false;
    }

    public boolean discoverServices(BluetoothGatt bluetoothGatt) {
        String str = TAG;
        Log.d(str, "Proceed to discover services");
        if (bluetoothGatt == null) {
            Log.e(str, "BluetoothGatt not initialized.");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            return bluetoothGatt.discoverServices();
        }
        Log.e(str, "Missing required permission to discover services");
        return false;
    }

    public boolean getCharacteristics(BluetoothGatt bluetoothGatt) {
        String str = TAG;
        Log.d(str, "Proceed to get characteristics");
        if (bluetoothGatt == null) {
            Log.e(str, "BluetoothGatt not initialized.");
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(serviceUUID);
        if (service == null) {
            Log.e(str, "Service not found");
            return false;
        }
        BluetoothLERemoteDevice bluetoothLERemoteDevice = this.mBluetoothLERemoteDeviceList.get(bluetoothGatt.getDevice().getAddress());
        if (bluetoothLERemoteDevice == null) {
            Log.e(str, "BluetoothLERemoteDevice not found.");
            return false;
        }
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        for (int i = 0; i < characteristics.size(); i++) {
            BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristics.get(i);
            if (bluetoothGattCharacteristic.getUuid().equals(rxCharacteristicUUID)) {
                Log.i(TAG, "Write characteristic found, UUID is: " + bluetoothGattCharacteristic.getUuid().toString());
                bluetoothLERemoteDevice.setRxCharacteristic(bluetoothGattCharacteristic);
            } else if (bluetoothGattCharacteristic.getUuid().equals(txCharacteristicUUID)) {
                Log.i(TAG, "Notify characteristic found, UUID is " + bluetoothGattCharacteristic.getUuid().toString());
                bluetoothLERemoteDevice.setTxCharacteristic(bluetoothGattCharacteristic);
            }
        }
        return bluetoothLERemoteDevice.getRxCharacteristic() != null && bluetoothLERemoteDevice.getTxCharacteristic() != null;
    }

    public boolean writeDescriptorEnableNotification(BluetoothGatt bluetoothGatt) {
        String str = TAG;
        Log.d(str, "Proceed to write descriptor to enable notification");
        if (bluetoothGatt == null) {
            Log.e(str, "BluetoothGatt not initialized.");
            return false;
        }
        BluetoothLERemoteDevice bluetoothLERemoteDevice = this.mBluetoothLERemoteDeviceList.get(bluetoothGatt.getDevice().getAddress());
        if (bluetoothLERemoteDevice == null) {
            Log.e(str, "BluetoothLERemoteDevice not found.");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            if (!bluetoothGatt.setCharacteristicNotification(bluetoothLERemoteDevice.getTxCharacteristic(), true)) {
                Log.e(str, "Failed setCharacteristicNotification txCharacteristic");
                return false;
            }
            BluetoothGattDescriptor descriptor = bluetoothLERemoteDevice.getTxCharacteristic().getDescriptor(descriptorUUID);
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                return bluetoothGatt.writeDescriptor(descriptor);
            }
            Log.e(str, "descriptor is null");
            return false;
        }
        Log.e(str, "Missing required permission to write descriptor to enable notification");
        return false;
    }

    public boolean updateMtu(BluetoothGatt bluetoothGatt) {
        String str = TAG;
        Log.d(str, "Proceed to update MTU");
        if (bluetoothGatt == null) {
            Log.e(str, "BluetoothGatt not initialized.");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            return bluetoothGatt.requestMtu(247);
        }
        Log.e(str, "Missing required permission to update MTU");
        return false;
    }

    public boolean readCharacteristicData(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (bluetoothGatt == null || bluetoothGattCharacteristic == null) {
            Log.e(TAG, "BluetoothGatt or BluetoothGattCharacteristic not initialized");
            return false;
        }
        byte[] value = bluetoothGattCharacteristic.getValue();
        if (value == null || value.length == 0) {
            return false;
        }
        Log.d(TAG, "Bluetooth LE Device " + bluetoothGatt.getDevice().getAddress() + " Data received: " + Utils.byteArrayToHexString(value));
        onDataReceived(bluetoothGatt.getDevice().getAddress(), value);
        return true;
    }

    public void onBonded(final String str, final String str2) {
        new Handler(Looper.getMainLooper()).post(() -> BluetoothLEManagerHelper.this.listenerOnBluetoothLEDeviceBonded(str, str2));
    }

    void listenerOnBluetoothLEDeviceBonded(String str, String str2) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEDeviceBonded(str, str2);
        }
    }

    public void onScan(final String str, final String str2) {
        new Handler(Looper.getMainLooper()).post(() -> BluetoothLEManagerHelper.this.listenerOnBluetoothLEDeviceScanned(str, str2));
    }

    void listenerOnBluetoothLEDeviceScanned(String str, String str2) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEDeviceScanned(str, str2);
        }
    }

    public void onConnect(final String str, final String str2) {
        new Handler(Looper.getMainLooper()).post(() -> BluetoothLEManagerHelper.this.listenerOnBluetoothLEDeviceConnected(str, str2));
    }

    void listenerOnBluetoothLEDeviceConnected(String str, String str2) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEDeviceConnected(str, str2);
        }
    }

    public void onDisconnect(final String str) {
        new Handler(Looper.getMainLooper()).post(() -> BluetoothLEManagerHelper.this.listenerOnBluetoothLEDeviceDisconnected(str));
    }

    void listenerOnBluetoothLEDeviceDisconnected(String str) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEDeviceDisconnected(str);
        }
    }

    private void onDataReceived(final String str, final byte[] bArr) {
        new Handler(Looper.getMainLooper()).post(() -> BluetoothLEManagerHelper.this.listenerOnBluetoothLEDataReceived(str, bArr));
    }

    void listenerOnBluetoothLEDataReceived(String str, byte[] bArr) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEDataReceived(str, bArr);
        }
    }

    public void onStateChanged(final int i) {
        new Handler(Looper.getMainLooper()).post(() -> BluetoothLEManagerHelper.this.listenerOnBluetoothLEStateChanged(i));
    }

    void listenerOnBluetoothLEStateChanged(int i) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEStateChanged(i);
        }
    }
}
