package com.growspace.sdk.uwb;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.uwb.RangingCapabilities;
import androidx.core.uwb.RangingParameters;
import androidx.core.uwb.RangingResult;
import androidx.core.uwb.UwbAddress;
import androidx.core.uwb.UwbComplexChannel;
import androidx.core.uwb.UwbControleeSessionScope;
import androidx.core.uwb.UwbControllerSessionScope;
import androidx.core.uwb.UwbDevice;
import androidx.core.uwb.UwbManager;
import androidx.core.uwb.rxjava3.UwbClientSessionScopeRx;
import androidx.core.uwb.rxjava3.UwbManagerRx;

import com.growspace.sdk.oob.model.UwbDeviceConfigData;
import com.growspace.sdk.oob.model.UwbPhoneConfigData;
import com.growspace.sdk.utils.Utils;
import com.growspace.sdk.uwb.model.UwbRemoteDevice;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.DisposableSubscriber;

public class UwbManagerHelper {
    private static final int CONTROLEE_ROLE = 1;
    private static final int CONTROLLER_ROLE = 0;
    private static final int PREFERRED_UWB_PHONE_ROLE = 1;
    private static final int PREFERRED_UWB_PROFILE_ID = 1;
    private static final String TAG = "UwbManagerHelper";
    private static final int UWB_CHANNEL = 9;
    private static final int UWB_PREAMBLE_INDEX = 10;
    private static final Map<String, Integer> uwbRoleMap;
    private final Context mContext;
    private UwbManager mUwbManager;
    private int mUwbChannel = 9;
    private int mUwbPreambleIndex = 10;
    private int mPreferredUwbProfileId = 1;
    private int mPreferredUwbPhoneRole = 1;
    private Listener mListener = null;
    private final HashMap<String, UwbRemoteDevice> mUwbRemoteDeviceList = new HashMap<>();

    public interface Listener {
        void onRangingCapabilities(RangingCapabilities rangingCapabilities);

        void onRangingComplete();

        void onRangingError(Throwable th);

        void onRangingResult(String str, RangingResult rangingResult);

        void onRangingStarted(String str, UwbPhoneConfigData uwbPhoneConfigData);
    }

    public boolean isEnabled() {
        return true;
    }

    static {
        HashMap<String, Integer> hashMap = new HashMap<>();
        uwbRoleMap = hashMap;
        hashMap.put("Controller", 0);
        hashMap.put("Controlee", 1);
    }

    public UwbManagerHelper(Context context) {
        this.mUwbManager = null;
        this.mContext = context;
        if (context.getPackageManager().hasSystemFeature("android.hardware.uwb")) {
            this.mUwbManager = UwbManager.createInstance(context);
        }
    }

    public void registerListener(Listener listener) {
        this.mListener = listener;
    }

    public void unregisterListener() {
        this.mListener = null;
    }

    public boolean isSupported() {
        return this.mUwbManager != null;
    }

    public void setUwbChannel(int i) {
        this.mUwbChannel = i;
    }

    public void setUwbPreambleIndex(int i) {
        this.mUwbPreambleIndex = i;
    }

    public void setPreferredUwbRole(String str) {
        Integer num = uwbRoleMap.get(str);
        if (num != null) {
            this.mPreferredUwbPhoneRole = num;
        }
    }

    public void setPreferredUwbProfileId(int i) {
        this.mPreferredUwbProfileId = i;
    }

    public boolean startRanging(final String str, final UwbDeviceConfigData uwbDeviceConfigData) {
        if (this.mUwbManager == null) {
            Log.e(TAG, "UWB Manager is not available in this device");
            return false;
        }
        if (str == null || str.isEmpty()) {
            Log.e(TAG, "remote address is not set");
            return false;
        }
        if (uwbDeviceConfigData == null) {
            Log.e(TAG, "uwbDeviceConfigData is not set");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.UWB_RANGING") == 0) {
            new Thread(() -> UwbManagerHelper.this.onStartRanging(uwbDeviceConfigData, str)).start();
            return true;
        }
        Log.e(TAG, "Missing required permission to start UWB ranging");
        return false;
    }

    void onStartRanging(UwbDeviceConfigData uwbDeviceConfigData, String str) {
        UwbAddress localAddress;
        UwbControllerSessionScope uwbControllerSessionScope;
        UwbComplexChannel uwbComplexChannel;
        UwbControleeSessionScope uwbControleeSessionScope;
        Flowable<RangingResult> rangingResultsFlowable;
        byte selectUwbDeviceRangingRole = selectUwbDeviceRangingRole(uwbDeviceConfigData.getSupportedDeviceRangingRoles());
        String str2 = TAG;
        Log.d(str2, "Uwb device supported ranging roles: " + ((int) uwbDeviceConfigData.getSupportedDeviceRangingRoles()) + ", selected role for UWB device: " + ((int) selectUwbDeviceRangingRole));
        byte selectUwbProfileId = selectUwbProfileId(uwbDeviceConfigData.getSupportedUwbProfileIds());
        Log.d(str2, "Uwb device supported UWB profile IDs: " + uwbDeviceConfigData.getSupportedUwbProfileIds() + ", selected UWB profile ID: " + ((int) selectUwbProfileId));
        try {
            if (selectUwbDeviceRangingRole == 0) {
                Log.d(str2, "Android device will act as Controlee!");
                UwbControleeSessionScope blockingGet = UwbManagerRx.controleeSessionScopeSingle(this.mUwbManager).blockingGet();
                localAddress = blockingGet.getLocalAddress();
                uwbComplexChannel = new UwbComplexChannel(this.mUwbChannel, this.mUwbPreambleIndex);
                uwbControllerSessionScope = null;
                uwbControleeSessionScope = blockingGet;
            } else {
                Log.d(str2, "Android device will act as Controller!");
                UwbControllerSessionScope blockingGet2 = UwbManagerRx.controllerSessionScopeSingle(this.mUwbManager).blockingGet();
                localAddress = blockingGet2.getLocalAddress();
                uwbControllerSessionScope = blockingGet2;
                uwbComplexChannel = blockingGet2.getUwbComplexChannel();
                uwbControleeSessionScope = null;
            }
            int nextInt = new Random().nextInt();
            UwbControleeSessionScope uwbControleeSessionScope2 = uwbControleeSessionScope;
            UwbDevice uwbDevice = new UwbDevice(new UwbAddress(uwbDeviceConfigData.getDeviceMacAddress()));
            ArrayList<UwbDevice> arrayList = new ArrayList<>();
            arrayList.add(uwbDevice);
            Log.d(str2, "UWB SessionId: " + nextInt);
            Log.d(str2, "UWB Local Address: " + localAddress);
            Log.d(str2, "UWB Remote Address: " + uwbDevice.getAddress());
            Log.d(str2, "UWB Channel: " + uwbComplexChannel.getChannel());
            Log.d(str2, "UWB Preamble Index: " + uwbComplexChannel.getPreambleIndex());
            byte[] hexStringToByteArray = Utils.hexStringtoByteArray("0807010203040506");
            Log.d(str2, "Configure ranging parameters for Profile ID: " + ((int) selectUwbProfileId));
            UwbAddress uwbAddress = localAddress;
            UwbControllerSessionScope uwbControllerSessionScope2 = uwbControllerSessionScope;
            RangingParameters rangingParameters = new RangingParameters(selectUwbProfileId, nextInt, 0, hexStringToByteArray, null, uwbComplexChannel, arrayList, 1);
            if (selectUwbDeviceRangingRole == 0) {
                Log.d(str2, "Configure controlee flowable");
                rangingResultsFlowable = UwbClientSessionScopeRx.rangingResultsFlowable(uwbControleeSessionScope2, rangingParameters);
            } else {
                Log.d(str2, "Configure controller flowable");
                rangingResultsFlowable = UwbClientSessionScopeRx.rangingResultsFlowable(uwbControllerSessionScope2, rangingParameters);
            }
            this.mUwbRemoteDeviceList.put(str, new UwbRemoteDevice(uwbDevice, rangingResultsFlowable.delay(100L, TimeUnit.MILLISECONDS).subscribeWith(new DisposableSubscriber<RangingResult>() {
                @Override
                public void onStart() {
                    request(1L);
                }

                @Override
                public void onNext(RangingResult rangingResult) {
                    String addressFromUwbDevice = UwbManagerHelper.this.getAddressFromUwbDevice(rangingResult.getDevice());
                    if (addressFromUwbDevice != null) {
                        UwbManagerHelper.this.onRangingResult(addressFromUwbDevice, rangingResult);
                    } else {
                        Log.e(UwbManagerHelper.TAG, "UWB ranging notification received for unexpected device address");
                    }
                    request(1L);
                }

                @Override
                public void onError(Throwable th) {
                    UwbManagerHelper.this.onRangingError(th);
                }

                @Override
                public void onComplete() {
                    UwbManagerHelper.this.onRangingComplete();
                }
            })));
            UwbPhoneConfigData uwbPhoneConfigData = new UwbPhoneConfigData();
            uwbPhoneConfigData.setSpecVerMajor((short) 256);
            uwbPhoneConfigData.setSpecVerMinor((short) 0);
            uwbPhoneConfigData.setSessionId(nextInt);
            uwbPhoneConfigData.setPreambleIndex((byte) uwbComplexChannel.getPreambleIndex());
            uwbPhoneConfigData.setChannel((byte) uwbComplexChannel.getChannel());
            uwbPhoneConfigData.setProfileId(selectUwbProfileId);
            uwbPhoneConfigData.setDeviceRangingRole((byte) (1 << selectUwbDeviceRangingRole));
            uwbPhoneConfigData.setPhoneMacAddress(uwbAddress.getAddress());
            onRangingStarted(str, uwbPhoneConfigData);
        } catch (Exception e) {
            Log.e(TAG, "UWB Ranging configuration exception: " + e.getMessage());
            onRangingError(e);
        }
    }

    public boolean stopRanging(String str) {
        String str2 = TAG;
        Log.d(str2, "Proceed to stop connection with device " + str);
        try {
            UwbRemoteDevice uwbRemoteDevice = this.mUwbRemoteDeviceList.get(str);
            if (uwbRemoteDevice != null && uwbRemoteDevice.getDisposable() != null) {
                uwbRemoteDevice.getDisposable().dispose();
                this.mUwbRemoteDeviceList.remove(str);
                return true;
            }
            Log.e(str2, "UWB Ranging session not started or disposable not initialized.");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Exception while closing UWB Ranging session: " + e.getMessage());
            return false;
        }
    }

    public boolean close(String str) {
        String str2 = TAG;
        Log.d(str2, "Proceed to close connection with device " + str);
        try {
            UwbRemoteDevice uwbRemoteDevice = this.mUwbRemoteDeviceList.get(str);
            if (uwbRemoteDevice != null && uwbRemoteDevice.getDisposable() != null) {
                uwbRemoteDevice.getDisposable().dispose();
                this.mUwbRemoteDeviceList.remove(str);
                return true;
            }
            Log.e(str2, "UWB Ranging session not started or disposable not initialized.");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Exception while closing UWB Ranging session: " + e.getMessage());
            return false;
        }
    }

    public boolean getRangingCapabilities() {
        if (this.mUwbManager == null) {
            Log.e(TAG, "UWB Manager is not available in this device");
            return false;
        }
        new Thread(UwbManagerHelper.this::onRangingCapabilities).start();
        return true;
    }

    void onRangingCapabilities() {
        try {
            onRangingCapabilities(UwbManagerRx.controleeSessionScopeSingle(this.mUwbManager).blockingGet().getRangingCapabilities());
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting UWB Ranging capabilities: " + e.getMessage());
            onRangingCapabilities(null);
        }
    }

    private byte selectUwbProfileId(int i) {
        if (BigInteger.valueOf(i).testBit(this.mPreferredUwbProfileId)) {
            return (byte) this.mPreferredUwbProfileId;
        }
        return BigInteger.valueOf(i).testBit(1) ? (byte) 1 : (byte) 0;
    }

    private byte selectUwbDeviceRangingRole(int i) {
        int i2 = this.mPreferredUwbPhoneRole;
        if (i2 != 0 || ((i >> 1) & 1) == 0) {
            return ((i2 != 1 || ((i) & 1) == 0) && ((i) & 1) == 0 && ((i >> 1) & 1) != 0) ? (byte) 1 : (byte) 0;
        }
        return (byte) 1;
    }

    public String getAddressFromUwbDevice(UwbDevice uwbDevice) {
        for (Map.Entry<String, UwbRemoteDevice> entry : this.mUwbRemoteDeviceList.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue().getUwbDevice().getAddress().toString().equals(uwbDevice.getAddress().toString())) {
                return key;
            }
        }
        return null;
    }

    private void onRangingStarted(final String str, final UwbPhoneConfigData uwbPhoneConfigData) {
        new Handler(Looper.getMainLooper()).post(() -> UwbManagerHelper.this.listenerOnRangingStarted(str, uwbPhoneConfigData));
    }

    void listenerOnRangingStarted(String str, UwbPhoneConfigData uwbPhoneConfigData) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onRangingStarted(str, uwbPhoneConfigData);
        }
    }

    public void onRangingResult(final String str, final RangingResult rangingResult) {
        new Handler(Looper.getMainLooper()).post(() -> UwbManagerHelper.this.listenerOnRangingResult(str, rangingResult));
    }

    void listenerOnRangingResult(String str, RangingResult rangingResult) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onRangingResult(str, rangingResult);
        }
    }

    public void onRangingError(final Throwable th) {
        new Handler(Looper.getMainLooper()).post(() -> UwbManagerHelper.this.listenerOnRangingError(th));
    }

    void listenerOnRangingError(Throwable th) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onRangingError(th);
        }
    }

    public void onRangingComplete() {
        new Handler(Looper.getMainLooper()).post(UwbManagerHelper.this::listenerOnRangingComplete);
    }

    void listenerOnRangingComplete() {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onRangingComplete();
        }
    }

    private void onRangingCapabilities(final RangingCapabilities rangingCapabilities) {
        new Handler(Looper.getMainLooper()).post(() -> UwbManagerHelper.this.listenerOnRangingCapabilities(rangingCapabilities));
    }

    void listenerOnRangingCapabilities(RangingCapabilities rangingCapabilities) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onRangingCapabilities(rangingCapabilities);
        }
    }
}
