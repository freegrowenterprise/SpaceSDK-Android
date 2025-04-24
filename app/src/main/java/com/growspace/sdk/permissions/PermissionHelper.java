package com.growspace.sdk.permissions;

import android.app.Activity;

import androidx.core.app.ActivityCompat;


import com.growspace.sdk.BaseObservable;

public class PermissionHelper extends BaseObservable<PermissionHelper.Listener> {
    private final Activity mActivity;

    public interface Listener {
        void onPermissionDeclined(String str, int i);

        void onPermissionDeclinedDontAskAgain(String str, int i);

        void onPermissionGranted(String str, int i);
    }

    public PermissionHelper(Activity activity) {
        this.mActivity = activity;
    }

    public boolean hasPermission(String str) {
        return ActivityCompat.checkSelfPermission(this.mActivity, str) == 0;
    }

    public void requestPermission(String str, int i) {
        ActivityCompat.requestPermissions(this.mActivity, new String[]{str}, i);
    }

    public void requestPermissions(String[] strArr, int i) {
        ActivityCompat.requestPermissions(this.mActivity, strArr, i);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (strArr.length < 1) {
            return;
        }
        for (int i2 = 0; i2 < strArr.length; i2++) {
            if (iArr[i2] == 0) {
                notifyPermissionGranted(strArr[i2], i);
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this.mActivity, strArr[i2])) {
                notifyPermissionDeclined(strArr[i2], i);
            } else {
                notifyPermissionDeclinedDontAskAgain(strArr[i2], i);
            }
        }
    }

    private void notifyPermissionDeclinedDontAskAgain(String str, int i) {
        for (Listener listener : getListeners()) {
            listener.onPermissionDeclinedDontAskAgain(str, i);
        }
    }

    private void notifyPermissionDeclined(String str, int i) {
        for (Listener listener : getListeners()) {
            listener.onPermissionDeclined(str, i);
        }
    }

    private void notifyPermissionGranted(String str, int i) {
        for (Listener listener : getListeners()) {
            listener.onPermissionGranted(str, i);
        }
    }
}
