package com.growspace.sdk.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

public class LocationManagerHelper {
    private static final String TAG = "LocationManagerHelper";
    private LocationManager locationManager;
    private Context mContext;
    private Listener mListener = null;
    private final BroadcastReceiver locationStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.location.MODE_CHANGED")) {
                LocationManagerHelper.this.onStateChanged(intent.getBooleanExtra("android.location.extra.LOCATION_ENABLED", false));
            }
        }
    };

    public interface Listener {
        void onLocationStateChanged(boolean z);
    }

    public LocationManagerHelper(Context context) {
        this.mContext = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean isSupported() {
        return this.locationManager != null;
    }

    public boolean isEnabled() {
        return this.locationManager.isProviderEnabled("gps") || this.locationManager.isProviderEnabled("network");
    }

    public void registerListener(Listener listener) {
        this.mListener = listener;
        this.mContext.registerReceiver(this.locationStateChangeReceiver, new IntentFilter("android.location.MODE_CHANGED"));
    }

    public void unregisterListener() {
        this.mListener = null;
        this.mContext.unregisterReceiver(this.locationStateChangeReceiver);
    }

    public void onStateChanged(final boolean z) {
        new Handler(Looper.getMainLooper()).post(() -> LocationManagerHelper.this.listenerOnLocationStateChanged(z));
    }

    void listenerOnLocationStateChanged(boolean z) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onLocationStateChanged(z);
        }
    }
}
