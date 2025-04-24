package com.growspace.sdk.bluetooth.model;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class BluetoothLERemoteDevice {
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic rxCharacteristic;
    private BluetoothGattCharacteristic txCharacteristic;

    public BluetoothLERemoteDevice() {
    }

    public BluetoothLERemoteDevice(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, BluetoothGattCharacteristic bluetoothGattCharacteristic2) {
        this.bluetoothGatt = bluetoothGatt;
        this.txCharacteristic = bluetoothGattCharacteristic;
        this.rxCharacteristic = bluetoothGattCharacteristic2;
    }

    public BluetoothGatt getBluetoothGatt() {
        return this.bluetoothGatt;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    public BluetoothGattCharacteristic getTxCharacteristic() {
        return this.txCharacteristic;
    }

    public void setTxCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.txCharacteristic = bluetoothGattCharacteristic;
    }

    public BluetoothGattCharacteristic getRxCharacteristic() {
        return this.rxCharacteristic;
    }

    public void setRxCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.rxCharacteristic = bluetoothGattCharacteristic;
    }
}
