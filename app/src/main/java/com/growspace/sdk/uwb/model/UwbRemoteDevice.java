package com.growspace.sdk.uwb.model;

import androidx.core.uwb.UwbDevice;

import io.reactivex.rxjava3.disposables.Disposable;

/* loaded from: classes2.dex */
public class UwbRemoteDevice {
    private Disposable disposable;
    private UwbDevice uwbDevice;

    public UwbRemoteDevice() {
    }

    public UwbRemoteDevice(UwbDevice uwbDevice, Disposable disposable) {
        this.uwbDevice = uwbDevice;
        this.disposable = disposable;
    }

    public UwbDevice getUwbDevice() {
        return this.uwbDevice;
    }

    public void setUwbDevice(UwbDevice uwbDevice) {
        this.uwbDevice = uwbDevice;
    }

    public Disposable getDisposable() {
        return this.disposable;
    }

    public void setDisposable(Disposable disposable) {
        this.disposable = disposable;
    }
}
