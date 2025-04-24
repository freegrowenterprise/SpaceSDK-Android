package com.growspace.sdk.oob.model;

import android.util.Log;

import com.growspace.sdk.utils.Utils;

import java.io.Serializable;

public class UwbPhoneConfigData implements Serializable {
    private static final String TAG = "UwbPhoneConfigData";
    byte channel;
    byte deviceRangingRole;
    byte[] phoneMacAddress;
    byte preambleIndex;
    byte profileId;
    int sessionId;
    short specVerMajor;
    short specVerMinor;

    public UwbPhoneConfigData() {
    }

    public UwbPhoneConfigData(short s, short s2, int i, byte b, byte b2, byte b3, byte b4, byte[] bArr) {
        this.specVerMajor = s;
        this.specVerMinor = s2;
        this.sessionId = i;
        this.preambleIndex = b;
        this.channel = b2;
        this.profileId = b3;
        this.deviceRangingRole = b4;
        this.phoneMacAddress = bArr;
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

    public int getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(int i) {
        this.sessionId = i;
    }

    public byte getPreambleIndex() {
        return this.preambleIndex;
    }

    public void setPreambleIndex(byte b) {
        this.preambleIndex = b;
    }

    public byte getChannel() {
        return this.channel;
    }

    public void setChannel(byte b) {
        this.channel = b;
    }

    public byte getProfileId() {
        return this.profileId;
    }

    public void setProfileId(byte b) {
        this.profileId = b;
    }

    public byte getDeviceRangingRole() {
        return this.deviceRangingRole;
    }

    public void setDeviceRangingRole(byte b) {
        this.deviceRangingRole = b;
    }

    public byte[] getPhoneMacAddress() {
        return this.phoneMacAddress;
    }

    public void setPhoneMacAddress(byte[] bArr) {
        this.phoneMacAddress = bArr;
    }

    public byte[] toByteArray() {
        return Utils.concat(Utils.concat(Utils.concat(Utils.concat(Utils.concat(Utils.concat(Utils.concat(Utils.concat(null, Utils.shortToByteArray(this.specVerMajor)), Utils.shortToByteArray(this.specVerMinor)), Utils.intToByteArray(this.sessionId)), Utils.byteToByteArray(this.preambleIndex)), Utils.byteToByteArray(this.channel)), Utils.byteToByteArray(this.profileId)), Utils.byteToByteArray(this.deviceRangingRole)), this.phoneMacAddress);
    }

    public static UwbPhoneConfigData fromByteArray(byte[] bArr) {
        try {
            UwbPhoneConfigData uwbPhoneConfigData = new UwbPhoneConfigData();
            uwbPhoneConfigData.setSpecVerMajor(Utils.byteArrayToShort(Utils.extract(bArr, 2, 0)));
            uwbPhoneConfigData.setSpecVerMinor(Utils.byteArrayToShort(Utils.extract(bArr, 2, 2)));
            uwbPhoneConfigData.setSessionId(Utils.byteArrayToShort(Utils.extract(bArr, 4, 4)));
            uwbPhoneConfigData.setPreambleIndex(Utils.byteArrayToByte(Utils.extract(bArr, 1, 8)));
            uwbPhoneConfigData.setChannel(Utils.byteArrayToByte(Utils.extract(bArr, 1, 9)));
            uwbPhoneConfigData.setProfileId(Utils.byteArrayToByte(Utils.extract(bArr, 1, 10)));
            uwbPhoneConfigData.setDeviceRangingRole(Utils.byteArrayToByte(Utils.extract(bArr, 1, 11)));
            uwbPhoneConfigData.setPhoneMacAddress(Utils.extract(bArr, 2, 12));
            return uwbPhoneConfigData;
        } catch (Exception unused) {
            Log.e(TAG, "Invalid data received!");
            return null;
        }
    }
}
