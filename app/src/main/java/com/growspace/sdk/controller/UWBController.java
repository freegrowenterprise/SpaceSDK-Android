package com.growspace.sdk.controller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.core.uwb.RangingCapabilities;
import androidx.core.uwb.RangingResult;

import com.growspace.sdk.model.UwbDisconnect;
import com.growspace.sdk.model.UwbRange;
import com.growspace.sdk.permissions.PermissionHelper;
import com.growspace.sdk.utils.Utils;
import com.growspace.sdk.model.Accessory;
import com.growspace.sdk.bluetooth.BluetoothLEManagerHelper;
import com.growspace.sdk.logger.LoggerHelper;
import com.growspace.sdk.oob.OoBHelper;
import com.growspace.sdk.oob.model.UwbDeviceConfigData;
import com.growspace.sdk.oob.model.UwbPhoneConfigData;

import com.growspace.sdk.location.LocationManagerHelper;
import com.growspace.sdk.uwb.UwbManagerHelper;

import com.growspace.sdk.storage.database.DatabaseStorageHelper;
import com.growspace.sdk.storage.preferences.PreferenceStorageHelper;


//import com.themobileknowledge.myusb.screens.common.actionhelper.ActionHelper;
//import com.themobileknowledge.myusb.screens.common.dialogs.DialogsEventBus;
//import com.themobileknowledge.myusb.screens.common.dialogs.DialogsManager;
//import com.themobileknowledge.myusb.screens.common.dialogs.editaccessorynamedialog.EditAccessoryAliasDialogEvent;
//import com.themobileknowledge.myusb.screens.common.dialogs.infodonotshowagaindialog.InfoDoNotShowAgainDialogEvent;
//import com.themobileknowledge.myusb.screens.common.dialogs.promptdialog.PromptDialogEvent;
//import com.themobileknowledge.myusb.screens.common.screensnavigator.ScreensNavigator;
//import com.themobileknowledge.myusb.screens.common.toastshelper.ToastsHelper;
//import com.themobileknowledge.myusb.screens.distancealert.DistanceAlertView;
//import com.themobileknowledge.myusb.screens.distancealert.adapters.DistanceAlertRecyclerItem;
//import com.themobileknowledge.myusb.screens.distancealert.dialogs.editdistancealertthresholdsdialog.EditDistanceAlertThresholdsDialogEvent;
//import com.themobileknowledge.myusb.screens.distancealert.listitem.DistanceAlertItemView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class UWBController implements
//        DistanceAlertView.Listener, DistanceAlertItemView.Listener, DialogsEventBus.Listener,
        BluetoothLEManagerHelper.Listener, UwbManagerHelper.Listener, LocationManagerHelper.Listener {
    private static final int BLE_CONNECT_TIMEOUT_MSECS = 5000;
    private static final String DIALOGTAG_BLUETOOTHNOTENABLED = "DIALOGTAG_BLUETOOTHNOTENABLED";
    private static final String DIALOGTAG_BLUETOOTHNOTSUPPORTED = "DIALOGTAG_BLUETOOTHNOTSUPPORTED";
    private static final String DIALOGTAG_CONFIRMCLOSEDEMO = "DIALOGTAG_CONFIRMCLOSEDEMO";
    private static final String DIALOGTAG_EDITACCESSORYNAME = "DIALOGTAG_EDITACCESSORYNAME";
    private static final String DIALOGTAG_EDITTHRESHOLDS = "DIALOGTAG_EDITTHRESHOLDS";
    private static final String DIALOGTAG_LOCATIONNOTENABLED = "DIALOGTAG_LOCATIONNOTENABLED";
    private static final String DIALOGTAG_LOCATIONNOTSUPPORTED = "DIALOGTAG_LOCATIONNOTSUPPORTED";
    private static final String DIALOGTAG_PAIRINGINFO = "DIALOGTAG_PAIRINGINFO";
    private static final String DIALOGTAG_REQUIREDPERMISSIONSMISSING = "DIALOGTAG_REQUIREDPERMISSIONSMISSING";
    private static final String DIALOGTAG_UWBNOTENABLED = "DIALOGTAG_UWBNOTENABLED";
    private static final String DIALOGTAG_UWBNOTSUPPORTED = "DIALOGTAG_UWBNOTSUPPORTED";
    private static final int LEGACY_OoB_SUPPORT_TIMEOUT_MSECS = 2000;
    private static final String LOG_DEMONAME = "DistanceAlert";
    private static final int MAX_ALLOWED_ACCESSORIES = 5;
    private static final String SAVED_STATE_SCREEN_STATE = "SAVED_STATE_SCREEN_STATE";
    private static final String TAG = "DistanceAlertController";
    //    private final ActionHelper mActionHelper;
    private final BluetoothLEManagerHelper mBluetoothLEManagerHelper;
    private final DatabaseStorageHelper mDatabaseStorageHelper;
    //    private final DialogsEventBus mDialogsEventBus;
//    private final DialogsManager mDialogsManager;
    private int mLimitCloseRangeThreshold;
    private int mLimitFarRangeThreshold;
    private final LocationManagerHelper mLocationManagerHelper;
    private final LoggerHelper mLoggerHelper;
    private Menu mMenu;
    private final PermissionHelper mPermissionHelper;
    private final PreferenceStorageHelper mPreferenceStorageHelper;
    //    private final ScreensNavigator mScreensNavigator;
//    private final ToastsHelper mToastsHelper;
    private final UwbManagerHelper mUwbManagerHelper;
    //    private DistanceAlertView mView;
    private Bundle mSavedInstanceState = null;
    //    private List<DistanceAlertRecyclerItem> mDistanceAlertItemList = new ArrayList();
    private List<Accessory> mAccessoriesList = new ArrayList();
    private List<Accessory> mAccessoriesConnectingList = new ArrayList();
    private HashMap<String, Timer> mTimerAccessoriesConnectList = new HashMap<>();
    private HashMap<String, Timer> mTimerAccessoriesLegacyOoBSupportList = new HashMap<>();
//    private ScreenState mScreenState = ScreenState.SCREEN_SHOWN;

//    private enum ScreenState {
//        SCREEN_SHOWN, CONFIRMCLOSEDEMO_DIALOG_SHOWN, PAIRINGINFO_DIALOG_SHOWN, DISTANCEALERTDEMO_RUNNING, REQUIREDPERMISSIONSMISSING_DIALOG_SHOWN, BLUETOOTHNOTSUPPORTED_DIALOG_SHOWN, UWBNOTSUPPORTED_DIALOG_SHOWN, LOCATIONNOTSUPPORTED_DIALOG_SHOWN, BLUETOOTHNOTENABLED_DIALOG_SHOWN, UWBNOTENABLED_DIALOG_SHOWN, LOCATIONNOTENABLED_DIALOG_SHOWN, EDITACCESSORYNAME_DIALOG_SHOWN, EDITTHRESHOLDS_DIALOG_SHOWN
//    }

    private Function1<? super UwbRange, Unit> onUpdate;
    private Function1<? super UwbDisconnect, Unit> onDisconnect;

    @Override
    public void onRangingCapabilities(RangingCapabilities rangingCapabilities) {
    }

    @Override
    public void onRangingComplete() {
    }

    public UWBController(
//            ScreensNavigator screensNavigator,
            PermissionHelper permissionHelper, PreferenceStorageHelper preferenceStorageHelper, DatabaseStorageHelper databaseStorageHelper,
//            ActionHelper actionHelper,
            LoggerHelper loggerHelper,
//            ToastsHelper toastsHelper,
            BluetoothLEManagerHelper bluetoothLEManagerHelper, LocationManagerHelper locationManagerHelper, UwbManagerHelper uwbManagerHelper
//            DialogsManager dialogsManager, DialogsEventBus dialogsEventBus
    ) {
//        this.mScreensNavigator = screensNavigator;
        this.mPermissionHelper = permissionHelper;
        this.mPreferenceStorageHelper = preferenceStorageHelper;
        this.mDatabaseStorageHelper = databaseStorageHelper;
//        this.mActionHelper = actionHelper;
        this.mLoggerHelper = loggerHelper;
//        this.mToastsHelper = toastsHelper;
        this.mBluetoothLEManagerHelper = bluetoothLEManagerHelper;
        this.mLocationManagerHelper = locationManagerHelper;
        this.mUwbManagerHelper = uwbManagerHelper;
//        this.mDialogsManager = dialogsManager;
//        this.mDialogsEventBus = dialogsEventBus;
    }

//    public void bindView(DistanceAlertView distanceAlertView) {
//        this.mView = distanceAlertView;
//    }

    public void setInstanceState(Bundle bundle) {
        this.mSavedInstanceState = bundle;
    }

//    public Bundle saveInstanceState(Bundle bundle) {
//        bundle.putSerializable(SAVED_STATE_SCREEN_STATE, this.mScreenState);
//        return bundle;
//    }

    public void onCreate() {
        this.mLoggerHelper.setDemoName(LOG_DEMONAME);
//        Bundle bundle = this.mSavedInstanceState;
//        if (bundle != null) {
//            this.mScreenState = (ScreenState) bundle.getSerializable(SAVED_STATE_SCREEN_STATE);
//        }
        applySettings();
        this.mLimitCloseRangeThreshold = this.mPreferenceStorageHelper.getDistanceAlertCloseRangeThreshold();
        this.mLimitFarRangeThreshold = this.mPreferenceStorageHelper.getDistanceAlertFarRangeThreshold();
//        this.mView.bindDistanceAlertItemList(this.mDistanceAlertItemList);
//        initializeRecyclerItemList();
        this.mBluetoothLEManagerHelper.registerListener(this);
        this.mLocationManagerHelper.registerListener(this);
        this.mUwbManagerHelper.registerListener(this);
    }

    public void onStart(
            ///  장치 최대 연결 개수. 디폴트 값 4.
            int maximumConnectionCount,

            /// 최대 연결 거리. 해당 값을 초과할 경우 연결을 끊고, 다른 UWB 장치와 연결 시도. 기본값 8(m)
            Float replacementDistanceThreshold,

            /// RSSI 신호가 강한 장치부터 연결 시도. 기본값 true.
            Boolean isConnectStrongestSignalFirst,

            Function1<? super UwbRange, Unit> onUpdate, Function1<? super UwbDisconnect, Unit> onDisconnect) {
//        if (this.mPreferenceStorageHelper.getShowPairingInfo()) {
//            showPairingInfoDialog();
//        } else {
//            startDistanceAlertDemo();
//        }
//        this.mView.registerListener(this);
//        this.mDialogsEventBus.registerListener(this);
        bleStartDeviceScan();
        log(LoggerHelper.LogEvent.LOG_EVENT_DEMO_START);

        this.onUpdate = onUpdate;
        this.onDisconnect = onDisconnect;
    }

    public boolean onStop() {
//        this.mView.unregisterListener(this);
//        this.mDialogsEventBus.unregisterListener(this);
        log(LoggerHelper.LogEvent.LOG_EVENT_DEMO_STOP);

        this.onUpdate = null;
        this.onDisconnect = null;

        return bleStopDeviceScan();
    }

//    @Override
//    public void onBackPressed() {
//        if (this.mAccessoriesList.size() > 0) {
//            this.mScreenState = ScreenState.CONFIRMCLOSEDEMO_DIALOG_SHOWN;
//            this.mDialogsManager.showConfirmCloseDemoDialog(DIALOGTAG_CONFIRMCLOSEDEMO);
//        } else {
//            this.mScreensNavigator.toSelectDemoMenu();
//        }
//    }

    public void onDestroy() {
        this.mBluetoothLEManagerHelper.unregisterListener();
        this.mLocationManagerHelper.unregisterListener();
        this.mUwbManagerHelper.unregisterListener();
        bleClose();
        uwbClose();
        cancelTimerBleConnect();
        cancelTimerAccessoriesLegacyOoBSupport();
        log(LoggerHelper.LogEvent.LOG_EVENT_DEMO_FINISHED);
    }

    public void onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
//        this.mView.bindMenu(menu);
    }

    public void onOptionsItemSelected(MenuItem menuItem) {
//        this.mView.onMenuItemSelected(menuItem);
    }

//    @Override
//    public void onMenuSettingsClicked() {
//        showEditDistanceAlertThresholdsDialog();
//    }

//    @Override
//    public void onAccessoryEditClicked(Accessory accessory) {
//        showEditAccessoryAliasDialog(accessory);
//    }

    @Override
    public void onLocationStateChanged(boolean z) {
        if (z) {
//            startDistanceAlertDemo();
            bleStartDeviceScan();
            return;
        }
        for (Accessory accessory : this.mAccessoriesList) {
            log(LoggerHelper.LogEvent.LOG_EVENT_BLE_DEV_DISCONNECTED, accessory);
        }
        bleClose();
        uwbClose();
        cancelTimerBleConnect();
        cancelTimerAccessoriesLegacyOoBSupport();
        this.mAccessoriesList.clear();
        this.mAccessoriesConnectingList.clear();
//        initializeRecyclerItemList();
//        updateDistanceAlertView();
//        this.mScreenState = ScreenState.SCREEN_SHOWN;
    }

    @Override
    public void onBluetoothLEStateChanged(int i) {
        if (i == 12) {
//            startDistanceAlertDemo();
            bleStartDeviceScan();
        }
        if (i == 10) {
            bleClose();
            uwbClose();
            cancelTimerBleConnect();
            cancelTimerAccessoriesLegacyOoBSupport();
            this.mAccessoriesList.clear();
            this.mAccessoriesConnectingList.clear();
//            initializeRecyclerItemList();
            bleStartDeviceScan();
//            updateDistanceAlertView();
//            this.mScreenState = ScreenState.SCREEN_SHOWN;
        }
    }

    @Override
    public void onBluetoothLEDeviceBonded(String str, String str2) {
        Accessory accessory = this.mDatabaseStorageHelper.getAccessory(str2);
        if (accessory == null) {
            accessory = new Accessory(str, str2, null);
        }
        for (Accessory value : this.mAccessoriesList) {
            if (accessory.getMac().equals(value.getMac())) {
                return;
            }
        }
        for (Accessory value : this.mAccessoriesConnectingList) {
            if (accessory.getMac().equals(value.getMac())) {
                return;
            }
        }
        this.mAccessoriesConnectingList.add(accessory);
        this.mBluetoothLEManagerHelper.connect(accessory.getMac());
        startTimerBleConnect(accessory);
    }

    @Override
    public void onBluetoothLEDeviceScanned(String str, String str2) {
        if (str == null || str.isEmpty() || str2 == null || str2.isEmpty()) {
            return;
        }
        Accessory accessory = this.mDatabaseStorageHelper.getAccessory(str2);
        if (accessory == null) {
            accessory = new Accessory(str, str2, null);
        }
        for (Accessory value : this.mAccessoriesList) {
            if (accessory.getMac().equals(value.getMac())) {
                return;
            }
        }
        for (Accessory value : this.mAccessoriesConnectingList) {
            if (accessory.getMac().equals(value.getMac())) {
                return;
            }
        }
        if (this.mAccessoriesList.size() + this.mAccessoriesConnectingList.size() >= 5) {
            return;
        }
        log(LoggerHelper.LogEvent.LOG_EVENT_BLE_DEV_SCANNED, accessory);
        log(LoggerHelper.LogEvent.LOG_EVENT_BLE_DEV_CONNECTING, accessory);
        this.mAccessoriesConnectingList.add(accessory);
        this.mBluetoothLEManagerHelper.connect(accessory.getMac());
        startTimerBleConnect(accessory);
    }

    @Override
    public void onBluetoothLEDeviceConnected(String str, String str2) {
        Accessory connectingAccessoryFromBluetoothLeAddress = getConnectingAccessoryFromBluetoothLeAddress(str2);
        if (connectingAccessoryFromBluetoothLeAddress == null) {
            connectingAccessoryFromBluetoothLeAddress = new Accessory(str, str2, null);
        }
        log(LoggerHelper.LogEvent.LOG_EVENT_BLE_DEV_CONNECTED, connectingAccessoryFromBluetoothLeAddress);
        this.mAccessoriesList.add(connectingAccessoryFromBluetoothLeAddress);
        this.mAccessoriesConnectingList.remove(connectingAccessoryFromBluetoothLeAddress);
        cancelTimerBleConnect(connectingAccessoryFromBluetoothLeAddress);
        transmitStartUwbRangingConfiguration(connectingAccessoryFromBluetoothLeAddress);
    }

    @Override
    public void onBluetoothLEDeviceDisconnected(String str) {
        Accessory accessoryFromBluetoothLeAddress = getAccessoryFromBluetoothLeAddress(str);
        if (accessoryFromBluetoothLeAddress == null) {
            Log.e(TAG, "Unexpected Bluetooth LE address");
            return;
        }
        bleClose(accessoryFromBluetoothLeAddress);
        uwbClose(accessoryFromBluetoothLeAddress);
        cancelTimerBleConnect(accessoryFromBluetoothLeAddress);
        cancelTimerAccessoriesLegacyOoBSupport(accessoryFromBluetoothLeAddress);
        this.mAccessoriesList.remove(accessoryFromBluetoothLeAddress);
//        Iterator<DistanceAlertRecyclerItem> it = this.mDistanceAlertItemList.iterator();
//        while (true) {
//            if (!it.hasNext()) {
//                break;
//            }
//            DistanceAlertRecyclerItem next = it.next();
//            if (!next.isThresholdLine() && next.getNotification().getAccessory().getMac().equals(accessoryFromBluetoothLeAddress.getMac())) {
//                this.mDistanceAlertItemList.remove(next);
//                break;
//            }
//        }
//        updateDistanceAlertView();
        showConnectionLostToast(accessoryFromBluetoothLeAddress);
    }

    @Override
    public void onBluetoothLEDataReceived(String str, byte[] bArr) {
        Accessory accessoryFromBluetoothLeAddress = getAccessoryFromBluetoothLeAddress(str);
        if (accessoryFromBluetoothLeAddress == null) {
            Log.e(TAG, "Unexpected Bluetooth LE address");
            return;
        }
        byte b = bArr[0];
        if (b == OoBHelper.MessageId.uwbDeviceConfigurationData.getMessageId()) {
            cancelTimerAccessoriesLegacyOoBSupport(accessoryFromBluetoothLeAddress);
            if (startRanging(accessoryFromBluetoothLeAddress, OoBHelper.getValue(bArr, OoBHelper.MessageId.uwbDeviceConfigurationData.getMessageId()))) {
                return;
            }
            invalidBluetoothLeDataReceived(accessoryFromBluetoothLeAddress);
            return;
        }
        if (b == OoBHelper.MessageId.uwbDidStart.getMessageId()) {
            uwbRangingSessionStarted(accessoryFromBluetoothLeAddress);
        } else if (b == OoBHelper.MessageId.uwbDidStop.getMessageId()) {
            uwbRangingSessionStopped(accessoryFromBluetoothLeAddress);
        } else {
            invalidBluetoothLeDataReceived(accessoryFromBluetoothLeAddress);
        }
    }

    @Override
    public void onRangingStarted(String str, UwbPhoneConfigData uwbPhoneConfigData) {
        Accessory accessoryFromBluetoothLeAddress = getAccessoryFromBluetoothLeAddress(str);
        if (accessoryFromBluetoothLeAddress == null) {
            Log.e(TAG, "Unexpected Bluetooth LE address");
        } else {
            transmitUwbPhoneConfigData(accessoryFromBluetoothLeAddress, uwbPhoneConfigData);
        }
    }

    @Override
    public void onRangingResult(String str, RangingResult rangingResult) {
        byte b;
        Accessory accessoryFromBluetoothLeAddress = getAccessoryFromBluetoothLeAddress(str);
        if (accessoryFromBluetoothLeAddress == null) {
            Log.e(TAG, "Unexpected Bluetooth LE address");
            return;
        }
        if (rangingResult instanceof RangingResult.RangingResultPosition) {
            RangingResult.RangingResultPosition rangingResultPosition = (RangingResult.RangingResultPosition) rangingResult;
            if (rangingResultPosition.getPosition().getDistance() == null || rangingResultPosition.getPosition().getAzimuth() == null) {
                return;
            }
            float value = rangingResultPosition.getPosition().getDistance().getValue();
            float value2 = rangingResultPosition.getPosition().getAzimuth().getValue();
            if (rangingResultPosition.getPosition().getElevation() != null) {
                log(LoggerHelper.LogEvent.LOG_EVENT_UWB_RANGING_RESULT, accessoryFromBluetoothLeAddress, String.valueOf((int) (value * 100.0f)), String.valueOf((int) value2), String.valueOf((int) rangingResultPosition.getPosition().getElevation().getValue()));
            } else {
                log(LoggerHelper.LogEvent.LOG_EVENT_UWB_RANGING_RESULT, accessoryFromBluetoothLeAddress, String.valueOf((int) (value * 100.0f)), String.valueOf((int) value2), "");
            }
            int i = (int) (value * 100.0f);
            if (i <= this.mLimitCloseRangeThreshold) {
                b = 2;
            } else {
                b = i >= this.mLimitFarRangeThreshold ? (byte) 0 : (byte) 1;
            }


            if (onUpdate != null) {
                UwbRange uwbRange = new UwbRange(accessoryFromBluetoothLeAddress.getName(), value, value2, rangingResultPosition.getPosition().getElevation() != null ? rangingResultPosition.getPosition().getElevation().getValue() : null);
                onUpdate.invoke(uwbRange);
            }
//            onDistanceAlertAccessoryNotification(new DistanceAlertNotification(accessoryFromBluetoothLeAddress, i, b));
            return;
        }
        if (rangingResult instanceof RangingResult.RangingResultPeerDisconnected) {
            bleClose(accessoryFromBluetoothLeAddress);
            uwbClose(accessoryFromBluetoothLeAddress);
            cancelTimerBleConnect(accessoryFromBluetoothLeAddress);
            cancelTimerAccessoriesLegacyOoBSupport(accessoryFromBluetoothLeAddress);
            this.mAccessoriesList.remove(accessoryFromBluetoothLeAddress);
//            Iterator<DistanceAlertRecyclerItem> it = this.mDistanceAlertItemList.iterator();
//            while (true) {
//                if (!it.hasNext()) {
//                    break;
//                }
//                DistanceAlertRecyclerItem next = it.next();
//                if (!next.isThresholdLine() && next.getNotification().getAccessory().getMac().equals(accessoryFromBluetoothLeAddress.getMac())) {
//                    this.mDistanceAlertItemList.remove(next);
//                    break;
//                }
//            }
//            updateDistanceAlertView();
            showConnectionLostToast(accessoryFromBluetoothLeAddress);
        }
    }

    @Override
    public void onRangingError(final Throwable th) {
        bleClose();
        uwbClose();
        cancelTimerBleConnect();
        cancelTimerAccessoriesLegacyOoBSupport();
        this.mAccessoriesList.clear();
        this.mAccessoriesConnectingList.clear();
//        initializeRecyclerItemList();
//        updateDistanceAlertView();
//        this.mScreenState = ScreenState.SCREEN_SHOWN;
        log(LoggerHelper.LogEvent.LOG_EVENT_UWB_RANGING_ERROR);
//        new Handler(Looper.getMainLooper()).post(() -> UWBController.this.m179x73423059(th));
    }

    private boolean checkPermissions() {
        Log.d(TAG, "checkPermissions: ");
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_ADMIN"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_SCAN"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_CONNECT"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.ACCESS_COARSE_LOCATION"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.ACCESS_FINE_LOCATION"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.UWB_RANGING"));

        return this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH") && this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_ADMIN") && this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_SCAN") && this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_CONNECT") && this.mPermissionHelper.hasPermission("android.permission.ACCESS_COARSE_LOCATION") && this.mPermissionHelper.hasPermission("android.permission.ACCESS_FINE_LOCATION") && this.mPermissionHelper.hasPermission("android.permission.UWB_RANGING");
    }

    private void applySettings() {
        this.mLoggerHelper.setLogsEnabled(this.mPreferenceStorageHelper.getLogsEnabled());
        this.mUwbManagerHelper.setUwbChannel(this.mPreferenceStorageHelper.getUwbChannel());
        this.mUwbManagerHelper.setUwbPreambleIndex(this.mPreferenceStorageHelper.getUwbPreambleIndex());
        this.mUwbManagerHelper.setPreferredUwbRole(this.mPreferenceStorageHelper.getUwbRole());
        this.mUwbManagerHelper.setPreferredUwbProfileId(this.mPreferenceStorageHelper.getUwbConfigType());
    }

    private boolean bleClose() {
        for (Accessory accessory : this.mAccessoriesList) {
            this.mBluetoothLEManagerHelper.close(accessory.getMac());
            log(LoggerHelper.LogEvent.LOG_EVENT_BLE_DEV_DISCONNECTED, accessory);
        }
        return true;
    }

    private boolean uwbClose() {
        for (Accessory accessory : this.mAccessoriesList) {
            this.mUwbManagerHelper.close(accessory.getMac());
            log(LoggerHelper.LogEvent.LOG_EVENT_UWB_RANGING_PEER_DISCONNECTED, accessory);
        }
        return true;
    }

    private boolean bleClose(Accessory accessory) {
        this.mBluetoothLEManagerHelper.close(accessory.getMac());
        log(LoggerHelper.LogEvent.LOG_EVENT_BLE_DEV_DISCONNECTED, accessory);
        return true;
    }

    private boolean uwbClose(Accessory accessory) {
        this.mUwbManagerHelper.close(accessory.getMac());
        log(LoggerHelper.LogEvent.LOG_EVENT_UWB_RANGING_PEER_DISCONNECTED, accessory);
        return true;
    }

//    private void startDistanceAlertDemo() {
//        this.mScreenState = ScreenState.DISTANCEALERTDEMO_RUNNING;
//        bleStartDeviceScan();
//    }

    private boolean bleStartDeviceScan() {
        if (!checkPermissions()) {
//            this.mScreenState = ScreenState.REQUIREDPERMISSIONSMISSING_DIALOG_SHOWN;
//            this.mDialogsManager.showRequiredPermissionsMissingDialog(DIALOGTAG_REQUIREDPERMISSIONSMISSING);
            return false;
        }
//        if (!this.mBluetoothLEManagerHelper.isSupported()) {
//            this.mScreenState = ScreenState.BLUETOOTHNOTSUPPORTED_DIALOG_SHOWN;
//            this.mDialogsManager.showBluetoothNotSupportedDialog(DIALOGTAG_BLUETOOTHNOTSUPPORTED);
//            return false;
//        }
//        if (!this.mUwbManagerHelper.isSupported()) {
//            this.mScreenState = ScreenState.UWBNOTSUPPORTED_DIALOG_SHOWN;
//            this.mDialogsManager.showUwbNotSupportedDialog(DIALOGTAG_UWBNOTSUPPORTED);
//            return false;
//        }
//        if (!this.mLocationManagerHelper.isSupported()) {
//            this.mScreenState = ScreenState.LOCATIONNOTSUPPORTED_DIALOG_SHOWN;
//            this.mDialogsManager.showLocationNotSupportedDialog(DIALOGTAG_LOCATIONNOTSUPPORTED);
//            return false;
//        }
//        if (!this.mBluetoothLEManagerHelper.isEnabled()) {
//            this.mScreenState = ScreenState.BLUETOOTHNOTENABLED_DIALOG_SHOWN;
//            this.mDialogsManager.showBluetoothNotEnabledDialog(DIALOGTAG_BLUETOOTHNOTENABLED);
//            return false;
//        }
//        if (!this.mUwbManagerHelper.isEnabled()) {
//            this.mScreenState = ScreenState.UWBNOTENABLED_DIALOG_SHOWN;
//            this.mDialogsManager.showUwbNotEnabledDialog(DIALOGTAG_UWBNOTENABLED);
//            return false;
//        }
//        if (!this.mLocationManagerHelper.isEnabled()) {
//            this.mScreenState = ScreenState.LOCATIONNOTENABLED_DIALOG_SHOWN;
//            this.mDialogsManager.showLocationNotEnabledDialog(DIALOGTAG_LOCATIONNOTENABLED);
//            return false;
//        }
        log(LoggerHelper.LogEvent.LOG_EVENT_BLE_SCAN_START);
        return this.mBluetoothLEManagerHelper.startLeDeviceScan();
    }

    private boolean bleStopDeviceScan() {
        log(LoggerHelper.LogEvent.LOG_EVENT_BLE_SCAN_STOP);
        return this.mBluetoothLEManagerHelper.stopLeDeviceScan();
    }

    private boolean transmitStartUwbRangingConfiguration(Accessory accessory) {
        byte[] buildOoBMessage = OoBHelper.buildOoBMessage(OoBHelper.MessageId.initialize.getMessageId());
        startTimerAccessoriesLegacyOoBSupport(accessory);
        return this.mBluetoothLEManagerHelper.transmit(accessory.getMac(), buildOoBMessage);
    }

    private boolean transmitLegacyStartUwbRangingConfiguration(Accessory accessory) {
        return this.mBluetoothLEManagerHelper.transmit(accessory.getMac(), OoBHelper.buildOoBMessage(OoBHelper.MessageIdLegacy.initialize.getMessageId(), Utils.byteToByteArray(OoBHelper.DevTypeLegacy.android.getValue())));
    }

    private boolean transmitUwbPhoneConfigData(Accessory accessory, UwbPhoneConfigData uwbPhoneConfigData) {
        return this.mBluetoothLEManagerHelper.transmit(accessory.getMac(), OoBHelper.buildOoBMessage(OoBHelper.MessageId.uwbPhoneConfigurationData.getMessageId(), uwbPhoneConfigData.toByteArray()));
    }

    private boolean transmitUwbRangingStop(Accessory accessory) {
        return this.mBluetoothLEManagerHelper.transmit(accessory.getMac(), OoBHelper.buildOoBMessage(OoBHelper.MessageId.stop.getMessageId()));
    }

    private boolean startRanging(Accessory accessory, byte[] bArr) {
        Log.d(TAG, "Start ranging with accessory: " + accessory.getMac());
        UwbDeviceConfigData fromByteArray = UwbDeviceConfigData.fromByteArray(bArr);
        if (fromByteArray != null) {
            return this.mUwbManagerHelper.startRanging(accessory.getMac(), fromByteArray);
        }
        return false;
    }

    private boolean stopRanging(Accessory accessory) {
        Log.d(TAG, "Stop ranging with accessory: " + accessory.getMac());
        return this.mUwbManagerHelper.stopRanging(accessory.getMac());
    }

    private void uwbRangingSessionStarted(Accessory accessory) {
        Log.d(TAG, "Ranging started with accessory: " + accessory.getMac());
        log(LoggerHelper.LogEvent.LOG_EVENT_UWB_RANGING_START);
    }

    private void uwbRangingSessionStopped(Accessory accessory) {
        Log.d(TAG, "Ranging stopped with accessory: " + accessory.getMac());
        log(LoggerHelper.LogEvent.LOG_EVENT_UWB_RANGING_STOP);
    }

    private void invalidBluetoothLeDataReceived(Accessory accessory) {
        bleClose(accessory);
        uwbClose(accessory);
        cancelTimerBleConnect(accessory);
        cancelTimerAccessoriesLegacyOoBSupport(accessory);
        this.mAccessoriesList.remove(accessory);
//        this.mToastsHelper.notifyGenericMessage("Invalid data received!");
    }

    private void cancelTimerBleConnect() {
        for (Timer timer : this.mTimerAccessoriesConnectList.values()) {
            timer.purge();
            timer.cancel();
        }
        this.mTimerAccessoriesConnectList.clear();
    }

    private void cancelTimerBleConnect(Accessory accessory) {
        Timer timer = this.mTimerAccessoriesConnectList.get(accessory.getMac());
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
        this.mTimerAccessoriesConnectList.remove(accessory.getMac());
    }

    private void startTimerBleConnect(final Accessory accessory) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(UWBController.TAG, "BluetoothLE Connect timeout fired!");
                UWBController.this.bleConnectTimeout(accessory.getMac());
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 5000L);
        this.mTimerAccessoriesConnectList.put(accessory.getMac(), timer);
    }

    private void cancelTimerAccessoriesLegacyOoBSupport() {
        for (Timer timer : this.mTimerAccessoriesLegacyOoBSupportList.values()) {
            timer.purge();
            timer.cancel();
        }
        this.mTimerAccessoriesConnectList.clear();
    }

    private void cancelTimerAccessoriesLegacyOoBSupport(Accessory accessory) {
        Timer timer = this.mTimerAccessoriesLegacyOoBSupportList.get(accessory.getMac());
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
        this.mTimerAccessoriesLegacyOoBSupportList.remove(accessory.getMac());
    }

    private void startTimerAccessoriesLegacyOoBSupport(final Accessory accessory) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(UWBController.TAG, "Legacy OoB support timeout fired!");
                UWBController.this.legacyOoBSupportTimeout(accessory.getMac());
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 2000L);
        this.mTimerAccessoriesLegacyOoBSupportList.put(accessory.getMac(), timer);
    }

    public void bleConnectTimeout(String str) {
        Accessory connectingAccessoryFromBluetoothLeAddress = getConnectingAccessoryFromBluetoothLeAddress(str);
        if (connectingAccessoryFromBluetoothLeAddress != null) {
            this.mAccessoriesConnectingList.remove(connectingAccessoryFromBluetoothLeAddress);
            cancelTimerBleConnect(connectingAccessoryFromBluetoothLeAddress);
        }
    }

    public void legacyOoBSupportTimeout(String str) {
        Accessory accessoryFromBluetoothLeAddress = getAccessoryFromBluetoothLeAddress(str);
        if (accessoryFromBluetoothLeAddress != null) {
            transmitLegacyStartUwbRangingConfiguration(accessoryFromBluetoothLeAddress);
            cancelTimerAccessoriesLegacyOoBSupport(accessoryFromBluetoothLeAddress);
        }
    }

//    private void initializeRecyclerItemList() {
//        this.mDistanceAlertItemList.clear();
//        this.mDistanceAlertItemList.add(new DistanceAlertRecyclerItem(this.mLimitFarRangeThreshold));
//        this.mDistanceAlertItemList.add(new DistanceAlertRecyclerItem(this.mLimitCloseRangeThreshold));
//    }


//    public void m179x73423059(Throwable th) {
//        th.printStackTrace();
//        this.mToastsHelper.notifyGenericMessage("UWB error, closed all opened sessions!");
//    }

//    private void onDistanceAlertAccessoryNotification(DistanceAlertNotification distanceAlertNotification) {
//        processNotification(distanceAlertNotification);
//        sortRecyclerItems();
//        updateDistanceAlertView();
//    }

//    private void processNotification(DistanceAlertNotification distanceAlertNotification) {
//        for (DistanceAlertRecyclerItem distanceAlertRecyclerItem : this.mDistanceAlertItemList) {
//            if (!distanceAlertRecyclerItem.isThresholdLine() && distanceAlertRecyclerItem.getNotification().getAccessory().getMac().equals(distanceAlertNotification.getAccessory().getMac())) {
//                distanceAlertRecyclerItem.setNotification(distanceAlertNotification);
//                return;
//            }
//        }
//        this.mDistanceAlertItemList.add(new DistanceAlertRecyclerItem(distanceAlertNotification));
//    }

//    private void sortRecyclerItems() {
//        Collections.sort(this.mDistanceAlertItemList, new Comparator<DistanceAlertRecyclerItem>() {
//            @Override // java.util.Comparator
//            public int compare(DistanceAlertRecyclerItem distanceAlertRecyclerItem, DistanceAlertRecyclerItem distanceAlertRecyclerItem2) {
//                int distance;
//                int distance2;
//                if (distanceAlertRecyclerItem.isThresholdLine() && distanceAlertRecyclerItem2.isThresholdLine()) {
//                    distance = distanceAlertRecyclerItem2.getThresholdLimit();
//                    distance2 = distanceAlertRecyclerItem.getThresholdLimit();
//                } else if (distanceAlertRecyclerItem.isThresholdLine()) {
//                    distance = distanceAlertRecyclerItem2.getNotification().getDistance();
//                    distance2 = distanceAlertRecyclerItem.getThresholdLimit();
//                } else if (distanceAlertRecyclerItem2.isThresholdLine()) {
//                    distance = distanceAlertRecyclerItem2.getThresholdLimit();
//                    distance2 = distanceAlertRecyclerItem.getNotification().getDistance();
//                } else {
//                    distance = distanceAlertRecyclerItem2.getNotification().getDistance();
//                    distance2 = distanceAlertRecyclerItem.getNotification().getDistance();
//                }
//                return distance - distance2;
//            }
//        });
//    }

//    private void showPairingInfoDialog() {
//        this.mScreenState = ScreenState.PAIRINGINFO_DIALOG_SHOWN;
//        this.mDialogsManager.showPairingInfoDialog(DIALOGTAG_PAIRINGINFO);
//    }

//    private void showEditDistanceAlertThresholdsDialog() {
//        this.mScreenState = ScreenState.EDITTHRESHOLDS_DIALOG_SHOWN;
//        this.mDialogsManager.showEditDistanceAlertThresholdsDialog(DIALOGTAG_EDITTHRESHOLDS, this.mLimitCloseRangeThreshold, this.mLimitFarRangeThreshold);
//    }

//    private void showEditAccessoryAliasDialog(Accessory accessory) {
//        this.mScreenState = ScreenState.EDITACCESSORYNAME_DIALOG_SHOWN;
//        this.mDialogsManager.showEditAccessoryAliasDialog(DIALOGTAG_EDITACCESSORYNAME, accessory);
//    }

    private void showConnectionLostToast(final Accessory accessory) {
        if (accessory != null) {
            // java.lang.Runnable
            new Handler(Looper.getMainLooper()).post(() -> UWBController.this.callShowConnectionLostToast(accessory));
        }
    }

    void callShowConnectionLostToast(Accessory accessory) {
//        if (accessory.getAlias() != null && !accessory.getAlias().isEmpty()) {
//            this.mToastsHelper.notifyGenericMessage("Connection lost with accessory: " + accessory.getAlias());
//        } else {
//            this.mToastsHelper.notifyGenericMessage("Connection lost with accessory: " + accessory.getName());
//        }
    }

//    private void updateDistanceAlertView() {
//        new Handler(Looper.getMainLooper()).post(() -> UWBController.this.mView.update());
//    }

    private Accessory getAccessoryFromBluetoothLeAddress(String str) {
        for (Accessory accessory : this.mAccessoriesList) {
            if (accessory.getMac().equals(str)) {
                return accessory;
            }
        }
        return null;
    }

    private Accessory getConnectingAccessoryFromBluetoothLeAddress(String str) {
        for (Accessory accessory : this.mAccessoriesConnectingList) {
            if (accessory.getMac().equals(str)) {
                return accessory;
            }
        }
        return null;
    }

    private void log(LoggerHelper.LogEvent logEvent) {
        this.mLoggerHelper.log(logEvent.toString());
    }

    private void log(LoggerHelper.LogEvent logEvent, Accessory accessory) {
        if (accessory.getAlias() == null || accessory.getAlias().isEmpty()) {
            this.mLoggerHelper.log(logEvent.toString(), accessory.getName(), accessory.getMac());
        } else {
            this.mLoggerHelper.log(logEvent.toString(), accessory.getAlias(), accessory.getMac());
        }
    }

    private void log(LoggerHelper.LogEvent logEvent, Accessory accessory, String str, String str2, String str3) {
        if (accessory.getAlias() == null || accessory.getAlias().isEmpty()) {
            this.mLoggerHelper.log(logEvent.toString(), accessory.getName(), accessory.getMac(), str, str2, str3);
        } else {
            this.mLoggerHelper.log(logEvent.toString(), accessory.getAlias(), accessory.getMac(), str, str2, str3);
        }
    }

//    @Override
//    public void onDialogEvent(Object obj) {
//        if (obj instanceof EditAccessoryAliasDialogEvent) {
//            EditAccessoryAliasDialogEvent editAccessoryAliasDialogEvent = (EditAccessoryAliasDialogEvent) obj;
//            if (AnonymousClass5.$SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$editaccessorynamedialog$EditAccessoryAliasDialogEvent$Button[editAccessoryAliasDialogEvent.getClickedButton().ordinal()] != 1) {
//                return;
//            }
//            Accessory accessory = editAccessoryAliasDialogEvent.getAccessory();
//            String accessoryAlias = editAccessoryAliasDialogEvent.getAccessoryAlias();
//            if (accessoryAlias == null || accessoryAlias.isEmpty()) {
//                this.mToastsHelper.notifyGenericMessage("Invalid alias!");
//                return;
//            }
//            accessory.setAlias(accessoryAlias);
//            if (this.mDatabaseStorageHelper.getAccessory(accessory.getMac()) == null) {
//                this.mDatabaseStorageHelper.insertAccessory(accessory);
//            } else {
//                this.mDatabaseStorageHelper.updateAccessoryAlias(accessory, accessoryAlias);
//            }
//            Iterator<Accessory> it = this.mAccessoriesList.iterator();
//            int i = 0;
//            while (it.hasNext()) {
//                if (accessory.getMac().equals(it.next().getMac())) {
//                    this.mAccessoriesList.set(i, accessory);
//                    return;
//                }
//                i++;
//            }
//            return;
//        }
//        if (obj instanceof EditDistanceAlertThresholdsDialogEvent) {
//            EditDistanceAlertThresholdsDialogEvent editDistanceAlertThresholdsDialogEvent = (EditDistanceAlertThresholdsDialogEvent) obj;
//            if (AnonymousClass5.$SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$dialogs$editdistancealertthresholdsdialog$EditDistanceAlertThresholdsDialogEvent$Button[editDistanceAlertThresholdsDialogEvent.getClickedButton().ordinal()] != 1) {
//                return;
//            }
//            int closeRangeThreshold = editDistanceAlertThresholdsDialogEvent.getCloseRangeThreshold();
//            int farRangeThreshold = editDistanceAlertThresholdsDialogEvent.getFarRangeThreshold();
//            if (closeRangeThreshold > 0 && farRangeThreshold > 0 && farRangeThreshold > closeRangeThreshold) {
//                this.mLimitCloseRangeThreshold = closeRangeThreshold;
//                this.mLimitFarRangeThreshold = farRangeThreshold;
//                this.mPreferenceStorageHelper.setDistanceAlertCloseRangeThreshold(closeRangeThreshold);
//                this.mPreferenceStorageHelper.setDistanceAlertFarRangeThreshold(farRangeThreshold);
//                initializeRecyclerItemList();
//                updateDistanceAlertView();
//                return;
//            }
//            this.mToastsHelper.notifyGenericMessage("Invalid thresholds!");
//            return;
//        }
//        if (obj instanceof PromptDialogEvent) {
//            switch (AnonymousClass5.$SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState[this.mScreenState.ordinal()]) {
//                case 1:
//                case 2:
//                case 3:
//                    this.mScreenState = ScreenState.SCREEN_SHOWN;
//                    break;
//                case 4:
//                    if (AnonymousClass5.$SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$promptdialog$PromptDialogEvent$Button[((PromptDialogEvent) obj).getClickedButton().ordinal()] == 1) {
//                        this.mActionHelper.enableBluetooth();
//                        break;
//                    } else {
//                        this.mScreenState = ScreenState.SCREEN_SHOWN;
//                        break;
//                    }
//                case 5:
//                    if (AnonymousClass5.$SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$promptdialog$PromptDialogEvent$Button[((PromptDialogEvent) obj).getClickedButton().ordinal()] == 1) {
//                        this.mActionHelper.enableLocation();
//                        break;
//                    } else {
//                        this.mScreenState = ScreenState.SCREEN_SHOWN;
//                        break;
//                    }
//                case 6:
//                    if (AnonymousClass5.$SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$promptdialog$PromptDialogEvent$Button[((PromptDialogEvent) obj).getClickedButton().ordinal()] == 1) {
//                        this.mActionHelper.enableUwb();
//                        break;
//                    } else {
//                        this.mScreenState = ScreenState.SCREEN_SHOWN;
//                        break;
//                    }
//                case 7:
//                    if (AnonymousClass5.$SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$promptdialog$PromptDialogEvent$Button[((PromptDialogEvent) obj).getClickedButton().ordinal()] == 1) {
//                        this.mScreensNavigator.toSelectDemoMenu();
//                        break;
//                    } else {
//                        this.mScreenState = ScreenState.SCREEN_SHOWN;
//                        break;
//                    }
//            }
//        }
//        if ((obj instanceof InfoDoNotShowAgainDialogEvent) && AnonymousClass5.$SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState[this.mScreenState.ordinal()] == 8) {
//            InfoDoNotShowAgainDialogEvent infoDoNotShowAgainDialogEvent = (InfoDoNotShowAgainDialogEvent) obj;
//            if (AnonymousClass5.$SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$infodonotshowagaindialog$InfoDoNotShowAgainDialogEvent$Button[infoDoNotShowAgainDialogEvent.getClickedButton().ordinal()] == 1) {
//                this.mPreferenceStorageHelper.setShowPairingInfo(infoDoNotShowAgainDialogEvent.getShowInfo());
//            }
//            startDistanceAlertDemo();
//        }
//    }


//    static class AnonymousClass5 {
//        static final int[] $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$editaccessorynamedialog$EditAccessoryAliasDialogEvent$Button;
//        static final int[] $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$infodonotshowagaindialog$InfoDoNotShowAgainDialogEvent$Button;
//        static final int[] $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$promptdialog$PromptDialogEvent$Button;
//        static final int[] $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState;
//        static final int[] $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$dialogs$editdistancealertthresholdsdialog$EditDistanceAlertThresholdsDialogEvent$Button;
//
//        static {
//            int[] iArr = new int[InfoDoNotShowAgainDialogEvent.Button.values().length];
//            $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$infodonotshowagaindialog$InfoDoNotShowAgainDialogEvent$Button = iArr;
//            try {
//                iArr[InfoDoNotShowAgainDialogEvent.Button.ACCEPT.ordinal()] = 1;
//            } catch (NoSuchFieldError unused) {
//            }
//            try {
//                $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$infodonotshowagaindialog$InfoDoNotShowAgainDialogEvent$Button[InfoDoNotShowAgainDialogEvent.Button.CANCEL.ordinal()] = 2;
//            } catch (NoSuchFieldError unused2) {
//            }
//            int[] iArr2 = new int[ScreenState.values().length];
//            $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState = iArr2;
//            try {
//                iArr2[ScreenState.BLUETOOTHNOTSUPPORTED_DIALOG_SHOWN.ordinal()] = 1;
//            } catch (NoSuchFieldError unused3) {
//            }
//            try {
//                $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState[ScreenState.UWBNOTSUPPORTED_DIALOG_SHOWN.ordinal()] = 2;
//            } catch (NoSuchFieldError unused4) {
//            }
//            try {
//                $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState[ScreenState.LOCATIONNOTSUPPORTED_DIALOG_SHOWN.ordinal()] = 3;
//            } catch (NoSuchFieldError unused5) {
//            }
//            try {
//                $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState[ScreenState.BLUETOOTHNOTENABLED_DIALOG_SHOWN.ordinal()] = 4;
//            } catch (NoSuchFieldError unused6) {
//            }
//            try {
//                $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState[ScreenState.LOCATIONNOTENABLED_DIALOG_SHOWN.ordinal()] = 5;
//            } catch (NoSuchFieldError unused7) {
//            }
//            try {
//                $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState[ScreenState.UWBNOTENABLED_DIALOG_SHOWN.ordinal()] = 6;
//            } catch (NoSuchFieldError unused8) {
//            }
//            try {
//                $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState[ScreenState.CONFIRMCLOSEDEMO_DIALOG_SHOWN.ordinal()] = 7;
//            } catch (NoSuchFieldError unused9) {
//            }
//            try {
//                $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$UWBController$ScreenState[ScreenState.PAIRINGINFO_DIALOG_SHOWN.ordinal()] = 8;
//            } catch (NoSuchFieldError unused10) {
//            }
//            int[] iArr3 = new int[PromptDialogEvent.Button.values().length];
//            $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$promptdialog$PromptDialogEvent$Button = iArr3;
//            try {
//                iArr3[PromptDialogEvent.Button.POSITIVE.ordinal()] = 1;
//            } catch (NoSuchFieldError unused11) {
//            }
//            int[] iArr4 = new int[EditDistanceAlertThresholdsDialogEvent.Button.values().length];
//            $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$dialogs$editdistancealertthresholdsdialog$EditDistanceAlertThresholdsDialogEvent$Button = iArr4;
//            try {
//                iArr4[EditDistanceAlertThresholdsDialogEvent.Button.EDIT.ordinal()] = 1;
//            } catch (NoSuchFieldError unused12) {
//            }
//            try {
//                $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$distancealert$dialogs$editdistancealertthresholdsdialog$EditDistanceAlertThresholdsDialogEvent$Button[EditDistanceAlertThresholdsDialogEvent.Button.CANCEL.ordinal()] = 2;
//            } catch (NoSuchFieldError unused13) {
//            }
//            int[] iArr5 = new int[EditAccessoryAliasDialogEvent.Button.values().length];
//            $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$editaccessorynamedialog$EditAccessoryAliasDialogEvent$Button = iArr5;
//            try {
//                iArr5[EditAccessoryAliasDialogEvent.Button.EDIT.ordinal()] = 1;
//            } catch (NoSuchFieldError unused14) {
//            }
//            try {
//                $SwitchMap$com$themobileknowledge$uwbconnectapp$screens$common$dialogs$editaccessorynamedialog$EditAccessoryAliasDialogEvent$Button[EditAccessoryAliasDialogEvent.Button.CANCEL.ordinal()] = 2;
//            } catch (NoSuchFieldError unused15) {
//            }
//        }
//    }
}
