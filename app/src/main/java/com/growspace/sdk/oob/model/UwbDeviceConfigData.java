package com.growspace.sdk.oob.model;

import android.util.Log;

import com.growspace.sdk.utils.Utils;

import java.io.Serializable;

public class UwbDeviceConfigData implements Serializable {
    private static final String TAG = "UwbDeviceConfigData";
    public byte[] chipFwVersion;
    public byte[] chipId;
    public byte[] deviceMacAddress;
    public byte[] mwVersion;
    public short specVerMajor;
    public short specVerMinor;
    public byte supportedDeviceRangingRoles;
    public int supportedUwbProfileIds;

    public UwbDeviceConfigData() {
        this.chipId = new byte[2];
        this.chipFwVersion = new byte[2];
        this.mwVersion = new byte[3];
    }

    public UwbDeviceConfigData(short s, short s2, byte[] bArr, byte[] bArr2, byte[] bArr3, int i, byte b, byte[] bArr4) {
        this.specVerMajor = s;
        this.specVerMinor = s2;
        this.chipId = bArr;
        this.chipFwVersion = bArr2;
        this.mwVersion = bArr3;
        this.supportedUwbProfileIds = i;
        this.supportedDeviceRangingRoles = b;
        this.deviceMacAddress = bArr4;
    }

    public short getSpecVerMajor() {
        return this.specVerMajor;
    }

    public void setSpecVerMajor(short s) {
        this.specVerMajor = s;
    }

    public short getSpecVerMinor() {
        return this.specVerMinor;
    }

    public void setSpecVerMinor(short s) {
        this.specVerMinor = s;
    }

    public byte[] getChipId() {
        return this.chipId;
    }

    public void setChipId(byte[] bArr) {
        this.chipId = bArr;
    }

    public byte[] getChipFwVersion() {
        return this.chipFwVersion;
    }

    public void setChipFwVersion(byte[] bArr) {
        this.chipFwVersion = bArr;
    }

    public byte[] getMwVersion() {
        return this.mwVersion;
    }

    public void setMwVersion(byte[] bArr) {
        this.mwVersion = bArr;
    }

    public int getSupportedUwbProfileIds() {
        return this.supportedUwbProfileIds;
    }

    public void setSupportedUwbProfileIds(int i) {
        this.supportedUwbProfileIds = i;
    }

    public byte getSupportedDeviceRangingRoles() {
        return this.supportedDeviceRangingRoles;
    }

    public void setSupportedDeviceRangingRoles(byte b) {
        this.supportedDeviceRangingRoles = b;
    }

    public byte[] getDeviceMacAddress() {
        return this.deviceMacAddress;
    }

    public void setDeviceMacAddress(byte[] bArr) {
        this.deviceMacAddress = bArr;
    }

    public byte[] toByteArray() {
        return Utils.concat(Utils.concat(Utils.concat(Utils.concat(Utils.concat(Utils.concat(Utils.concat(Utils.concat(null, Utils.shortToByteArray(this.specVerMajor)), Utils.shortToByteArray(this.specVerMinor)), this.chipId), this.chipFwVersion), this.mwVersion), Utils.intToByteArray(this.supportedUwbProfileIds)), Utils.byteToByteArray(this.supportedDeviceRangingRoles)), this.deviceMacAddress);
    }

    public static UwbDeviceConfigData fromByteArray(byte[] bArr) {
        try {
            UwbDeviceConfigData uwbDeviceConfigData = new UwbDeviceConfigData();
            uwbDeviceConfigData.setSpecVerMajor(Utils.byteArrayToShort(Utils.extract(bArr, 2, 0)));
            uwbDeviceConfigData.setSpecVerMinor(Utils.byteArrayToShort(Utils.extract(bArr, 2, 2)));
            uwbDeviceConfigData.setChipId(Utils.extract(bArr, 2, 4));
            uwbDeviceConfigData.setChipFwVersion(Utils.extract(bArr, 2, 6));
            uwbDeviceConfigData.setMwVersion(Utils.extract(bArr, 3, 8));
            uwbDeviceConfigData.setSupportedUwbProfileIds(Utils.byteArrayToInt(Utils.extract(bArr, 4, 11)));
            uwbDeviceConfigData.setSupportedDeviceRangingRoles(Utils.byteArrayToByte(Utils.extract(bArr, 1, 15)));
            uwbDeviceConfigData.setDeviceMacAddress(Utils.extract(bArr, 2, 16));
            return uwbDeviceConfigData;
        } catch (Exception unused) {
            Log.e(TAG, "Invalid data received!");
            return null;
        }
    }
}
