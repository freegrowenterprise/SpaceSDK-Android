/**
 * UWBController 클래스는 UWB(Ultra-Wideband) 통신의 핵심 컨트롤러 클래스입니다.
 * 이 클래스는 블루투스 LE, UWB, 위치 서비스 등의 통합 관리를 담당하며,
 * 장치 검색, 연결, 거리 측정 등의 기능을 제공합니다.
 *
 * 주요 기능:
 * 1. 블루투스 LE 장치 검색 및 연결 관리
 * 2. UWB 거리 측정 세션 관리
 * 3. 위치 서비스 상태 모니터링
 * 4. 액세서리 연결 및 데이터 전송 관리
 * 5. 거리 기반 알림 처리
 *
 * 사용 방법:
 * 1. UWBController 인스턴스 생성
 * 2. onCreate() 호출하여 초기화
 * 3. onStart() 호출하여 서비스 시작
 * 4. 거리 측정 결과 수신 (onUpdate 콜백)
 * 5. 연결 해제 이벤트 수신 (onDisconnect 콜백)
 * 6. onStop() 호출하여 서비스 중지
 * 7. onDestroy() 호출하여 리소스 정리
 */
package com.growspace.sdk.controller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;

import androidx.core.uwb.RangingCapabilities;
import androidx.core.uwb.RangingResult;

import com.growspace.sdk.model.DisconnectType;
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

/**
 * UWBController는 블루투스 LE, UWB, 위치 서비스의 리스너를 구현하여
 * 각 서비스의 상태 변화와 이벤트를 처리합니다.
 */
public class UWBController implements
        BluetoothLEManagerHelper.Listener, UwbManagerHelper.Listener, LocationManagerHelper.Listener {
    /**
     * 블루투스 LE 연결 타임아웃 시간 (밀리초)
     * 5초 동안 연결이 되지 않으면 타임아웃 처리
     */
    private static final int BLE_CONNECT_TIMEOUT_MSECS = 5000;

    /**
     * 로그 데모 이름
     * 로깅 시 사용되는 데모 식별자
     */
    private static final String LOG_DEMONAME = "DistanceAlert";

    /**
     * 최대 허용 액세서리 수
     * 동시에 연결할 수 있는 최대 액세서리 수
     */
    private static final int MAX_ALLOWED_ACCESSORIES = 5;

    /**
     * 로그 태그
     * 로깅 시 사용되는 태그 식별자
     */
    private static final String TAG = "DistanceAlertController";

    /**
     * 블루투스 LE 관리자 헬퍼
     * 블루투스 LE 장치 검색, 연결, 데이터 전송 등을 관리
     */
    private final BluetoothLEManagerHelper mBluetoothLEManagerHelper;

    /**
     * 근접 범위 임계값
     * 액세서리가 이 거리 이내로 접근하면 근접 상태로 판단
     */
    private int mLimitCloseRangeThreshold;

    /**
     * 원거리 범위 임계값
     * 액세서리가 이 거리 이상으로 멀어지면 원거리 상태로 판단
     */
    private int mLimitFarRangeThreshold;

    /**
     * 위치 관리자 헬퍼
     * 위치 서비스 상태 및 권한 관리
     */
    private final LocationManagerHelper mLocationManagerHelper;

    /**
     * 로거 헬퍼
     * 로깅 관련 기능 제공
     */
    private final LoggerHelper mLoggerHelper;

    /**
     * 권한 관리 헬퍼
     * 필요한 권한 확인 및 요청 관리
     */
    private final PermissionHelper mPermissionHelper;

    /**
     * 설정 저장소 헬퍼
     * 앱 설정값 관리
     */
    private final PreferenceStorageHelper mPreferenceStorageHelper;

    /**
     * UWB 관리자 헬퍼
     * UWB 거리 측정 세션 관리
     */
    private final UwbManagerHelper mUwbManagerHelper;

    /**
     * 저장된 인스턴스 상태
     * 액티비티 상태 저장 및 복원에 사용
     */
    private Bundle mSavedInstanceState = null;
    // private List<DistanceAlertRecyclerItem> mDistanceAlertItemList = new ArrayList();

    /**
     * 연결된 액세서리 목록
     * 현재 연결된 모든 액세서리 정보 관리
     */
    private List<Accessory> mAccessoriesList = new ArrayList();

    /**
     * 연결 중인 액세서리 목록
     * 현재 연결 시도 중인 액세서리 정보 관리
     */
    private List<Accessory> mAccessoriesConnectingList = new ArrayList();

    /**
     * 블루투스 LE 연결 타이머 목록
     * 각 액세서리의 연결 타임아웃 관리
     */
    private final HashMap<String, Timer> mTimerAccessoriesConnectList = new HashMap<>();

    /**
     * 레거시 OoB 지원 타이머 목록
     * 레거시 장치 지원을 위한 타임아웃 관리
     */
    private HashMap<String, Timer> mTimerAccessoriesLegacyOoBSupportList = new HashMap<>();
//    private ScreenState mScreenState = ScreenState.SCREEN_SHOWN;

//    private enum ScreenState {
//        SCREEN_SHOWN, CONFIRMCLOSEDEMO_DIALOG_SHOWN, PAIRINGINFO_DIALOG_SHOWN, DISTANCEALERTDEMO_RUNNING, REQUIREDPERMISSIONSMISSING_DIALOG_SHOWN, BLUETOOTHNOTSUPPORTED_DIALOG_SHOWN, UWBNOTSUPPORTED_DIALOG_SHOWN, LOCATIONNOTSUPPORTED_DIALOG_SHOWN, BLUETOOTHNOTENABLED_DIALOG_SHOWN, UWBNOTENABLED_DIALOG_SHOWN, LOCATIONNOTENABLED_DIALOG_SHOWN, EDITACCESSORYNAME_DIALOG_SHOWN, EDITTHRESHOLDS_DIALOG_SHOWN
//    }

    /**
     * 거리 측정 결과 콜백
     * 새로운 거리 측정 결과가 도착할 때 호출되는 콜백
     */
    private Function1<? super UwbRange, Unit> onUpdate;

    /**
     * UWB 연결 해제 콜백
     * 연결이 해제될 때 호출되는 콜백
     */
    private Function1<? super UwbDisconnect, Unit> onDisconnect;

    /**
     * UWB 거리 측정 기능 정보를 받는 콜백 메서드
     * @param rangingCapabilities UWB 거리 측정 기능 정보
     */
    @Override
    public void onRangingCapabilities(RangingCapabilities rangingCapabilities) {
        Log.e("UWB !!", "onRangingCapabilities: " + rangingCapabilities);
    }

    /**
     * UWB 거리 측정이 완료되었을 때 호출되는 콜백 메서드
     */
    @Override
    public void onRangingComplete() {
    }

    /**
     * UWBController 생성자
     * 필요한 모든 헬퍼 클래스들을 초기화합니다.
     *
     * @param permissionHelper 권한 관리 헬퍼
     * @param preferenceStorageHelper 설정 저장소 헬퍼
     * @param loggerHelper 로거 헬퍼
     * @param bluetoothLEManagerHelper 블루투스 LE 관리자 헬퍼
     * @param locationManagerHelper 위치 관리자 헬퍼
     * @param uwbManagerHelper UWB 관리자 헬퍼
     */
    public UWBController(
            PermissionHelper permissionHelper, PreferenceStorageHelper preferenceStorageHelper,
            LoggerHelper loggerHelper,
            BluetoothLEManagerHelper bluetoothLEManagerHelper, LocationManagerHelper locationManagerHelper, UwbManagerHelper uwbManagerHelper
    ) {
        this.mPermissionHelper = permissionHelper;
        this.mPreferenceStorageHelper = preferenceStorageHelper;
        this.mLoggerHelper = loggerHelper;
        this.mBluetoothLEManagerHelper = bluetoothLEManagerHelper;
        this.mLocationManagerHelper = locationManagerHelper;
        this.mUwbManagerHelper = uwbManagerHelper;
    }

    /**
     * 컨트롤러 초기화 메서드
     * 필요한 설정을 적용하고 리스너를 등록합니다.
     *
     * 처리 단계:
     * 1. 로그 데모 이름 설정
     * 2. 저장된 상태 복원 (있는 경우)
     * 3. 설정 적용
     * 4. 거리 임계값 설정
     * 5. 리스너 등록
     */
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

    /**
     * UWB 거리 측정을 시작하는 메서드
     *
     * @param maximumConnectionCount 최대 연결 가능한 장치 수
     * @param replacementDistanceThreshold 연결 해제 거리 임계값
     * @param isConnectStrongestSignalFirst 강한 신호 장치 우선 연결 여부
     * @param onUpdate 거리 측정 결과 콜백
     * @param onDisconnect 연결 해제 콜백
     *
     * 처리 단계:
     * 1. 블루투스 LE 장치 스캔 시작
     * 2. 로그 이벤트 기록
     * 3. 콜백 함수 설정
     */
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

    /**
     * UWB 거리 측정을 중지하는 메서드
     *
     * @return 중지 성공 여부
     *
     * 처리 단계:
     * 1. 블루투스 LE 장치 스캔 중지
     * 2. 로그 이벤트 기록
     * 3. 연결 해제 콜백 함수 초기화
     * 4. 연결된 액세서리 목록 순회하여 로그 기록
     * 5. 블루투스 LE 및 UWB 연결 종료
     * 6. 타이머 취소
     * 7. 액세서리 목록 초기화
     * 8. 액세서리 연결 목록 초기화
     * 9. 액세서리 연결 해제
     */
    public boolean onStop() {
//        this.mView.unregisterListener(this);
//        this.mDialogsEventBus.unregisterListener(this);
        log(LoggerHelper.LogEvent.LOG_EVENT_DEMO_STOP);

        for (Accessory accessory : this.mAccessoriesList) {
            log(LoggerHelper.LogEvent.LOG_EVENT_BLE_DEV_DISCONNECTED, accessory);
        }
        boolean isBleClose = bleClose();
        boolean isUwbClose = uwbClose();
        cancelTimerBleConnect();
        cancelTimerAccessoriesLegacyOoBSupport();
        this.mAccessoriesList.clear();
        this.mAccessoriesConnectingList.clear();
        bleStopDeviceScan();

        this.onUpdate = null;
        this.onDisconnect = null;

        return isBleClose && isUwbClose;
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


    /**
     * 컨트롤러 종료 메서드
     * 모든 리소스를 정리하고 연결을 해제합니다.
     *
     * 처리 단계:
     * 1. 리스너 등록 해제
     * 2. 블루투스 LE 연결 종료
     * 3. UWB 연결 종료
     * 4. 타이머 취소
     * 5. 로그 이벤트 기록
     */
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

    public void onOptionsItemSelected(MenuItem menuItem) {
//        this.mView.onMenuItemSelected(menuItem);
    }

    /**
     * 위치 서비스 상태 변경 콜백
     *
     * @param z 위치 서비스 활성화 여부
     *
     * 처리 단계:
     * 1. 위치 서비스 활성화 시:
     *    - 블루투스 LE 장치 스캔 시작
     * 2. 위치 서비스 비활성화 시:
     *    - 모든 연결 해제
     *    - 리소스 정리
     */
    @Override
    public void onLocationStateChanged(boolean z) {
        if (z) {
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
    }

    /**
     * 블루투스 LE 상태 변경 콜백
     *
     * @param i 블루투스 LE 상태 코드
     *
     * 처리 단계:
     * 1. 상태 코드 12 (활성화) 시:
     *    - 블루투스 LE 장치 스캔 시작
     * 2. 상태 코드 10 (비활성화) 시:
     *    - 모든 연결 해제
     *    - 리소스 정리
     *    - 스캔 재시작
     */
    @Override
    public void onBluetoothLEStateChanged(int i) {
        if (i == 12) {
            bleStartDeviceScan();
        }
        if (i == 10) {
            bleClose();
            uwbClose();
            cancelTimerBleConnect();
            cancelTimerAccessoriesLegacyOoBSupport();
            this.mAccessoriesList.clear();
            this.mAccessoriesConnectingList.clear();
            bleStartDeviceScan();
        }
    }

    @Override
    public void onBluetoothLEDeviceBonded(String str, String str2) {
        Accessory accessory = new Accessory(str, str2, null);

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
        Accessory accessory = new Accessory(str, str2, null);
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

        if (onDisconnect != null) {
            UwbDisconnect uwbDisconnect = new UwbDisconnect(DisconnectType.DISCONNECTED_DUE_TO_SYSTEM, accessoryFromBluetoothLeAddress.getName());
            onDisconnect.invoke(uwbDisconnect);
        }
    }

    /**
     * 블루투스 LE 데이터 수신 처리 메서드
     *
     * @param str 액세서리의 MAC 주소
     * @param bArr 수신된 데이터
     *
     * 처리 단계:
     * 1. 액세서리 찾기
     * 2. 메시지 ID 확인
     * 3. 메시지 타입에 따른 처리:
     *    - UWB 장치 설정 데이터: 거리 측정 시작
     *    - UWB 시작/중지: 세션 상태 업데이트
     *    - 기타: 잘못된 데이터 처리
     */
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

    /**
     * UWB 거리 측정 결과 처리 메서드
     *
     * @param str 액세서리의 MAC 주소
     * @param rangingResult 거리 측정 결과
     *
     * 처리 단계:
     * 1. 액세서리 찾기
     * 2. 결과 타입에 따른 처리:
     *    - 위치 결과: 거리 및 각도 정보 처리
     *    - 연결 해제: 리소스 정리 및 콜백 호출
     */
    @Override
    public void onRangingResult(String str, RangingResult rangingResult) {
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
                log(accessoryFromBluetoothLeAddress, String.valueOf((int) (value * 100.0f)), String.valueOf((int) value2), String.valueOf((int) rangingResultPosition.getPosition().getElevation().getValue()));
            } else {
                log(accessoryFromBluetoothLeAddress, String.valueOf((int) (value * 100.0f)), String.valueOf((int) value2), "");
            }
            int i = (int) (value * 100.0f);
            byte b;
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
            if (onDisconnect != null) {
                UwbDisconnect uwbDisconnect = new UwbDisconnect(DisconnectType.DISCONNECTED_DUE_TO_SYSTEM, accessoryFromBluetoothLeAddress.getName());
                onDisconnect.invoke(uwbDisconnect);
            }
        }
    }

    /**
     * UWB 거리 측정 오류 처리 메서드
     *
     * @param th 발생한 오류
     *
     * 처리 단계:
     * 1. 모든 연결 해제
     * 2. 리소스 정리
     * 3. 로그 이벤트 기록
     */
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

    /**
     * 필요한 권한 확인 메서드
     *
     * @return 모든 권한이 있는지 여부
     *
     * 처리 단계:
     * 1. 각 권한 확인
     * 2. 로그 기록
     * 3. 모든 권한 존재 여부 반환
     */
    private boolean checkPermissions() {
        Log.d(TAG, "checkPermissions: ");
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_ADMIN"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_SCAN"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_CONNECT"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.ACCESS_COARSE_LOCATION"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.ACCESS_FINE_LOCATION"));
        Log.d(TAG, "checkPermissions: " + this.mPermissionHelper.hasPermission("android.permission.UWB_RANGING"));

        return this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH") &&
               this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_ADMIN") &&
               this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_SCAN") &&
               this.mPermissionHelper.hasPermission("android.permission.BLUETOOTH_CONNECT") &&
               this.mPermissionHelper.hasPermission("android.permission.ACCESS_COARSE_LOCATION") &&
               this.mPermissionHelper.hasPermission("android.permission.ACCESS_FINE_LOCATION") &&
               this.mPermissionHelper.hasPermission("android.permission.UWB_RANGING");
    }

    /**
     * 설정 적용 메서드
     *
     * 처리 단계:
     * 1. 로그 활성화 설정
     * 2. UWB 채널 설정
     * 3. UWB 프리앰블 인덱스 설정
     * 4. UWB 역할 설정
     * 5. UWB 프로파일 ID 설정
     */
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
            return false;
        }
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

    /**
     * UWB 거리 측정 시작 메서드
     *
     * @param accessory 거리 측정을 시작할 액세서리
     * @param bArr UWB 장치 설정 데이터
     * @return 시작 성공 여부
     *
     * 처리 단계:
     * 1. 로그 기록
     * 2. UWB 장치 설정 데이터 파싱
     * 3. UWB 거리 측정 시작
     */
    private boolean startRanging(Accessory accessory, byte[] bArr) {
        Log.d(TAG, "Start ranging with accessory: " + accessory.getMac());
        UwbDeviceConfigData fromByteArray = UwbDeviceConfigData.fromByteArray(bArr);
        if (fromByteArray != null) {
            return this.mUwbManagerHelper.startRanging(accessory.getMac(), fromByteArray);
        }
        return false;
    }

    /**
     * UWB 거리 측정 중지 메서드
     *
     * @param accessory 거리 측정을 중지할 액세서리
     * @return 중지 성공 여부
     *
     * 처리 단계:
     * 1. 로그 기록
     * 2. UWB 거리 측정 중지
     */
    private boolean stopRanging(Accessory accessory) {
        Log.d(TAG, "Stop ranging with accessory: " + accessory.getMac());
        return this.mUwbManagerHelper.stopRanging(accessory.getMac());
    }

    /**
     * UWB 거리 측정 세션 시작 처리 메서드
     *
     * @param accessory 거리 측정을 시작한 액세서리
     *
     * 처리 단계:
     * 1. 로그 기록
     * 2. 거리 측정 시작 이벤트 로깅
     */
    private void uwbRangingSessionStarted(Accessory accessory) {
        Log.d(TAG, "Ranging started with accessory: " + accessory.getMac());
        log(LoggerHelper.LogEvent.LOG_EVENT_UWB_RANGING_START);
    }

    /**
     * UWB 거리 측정 세션 중지 처리 메서드
     *
     * @param accessory 거리 측정을 중지한 액세서리
     *
     * 처리 단계:
     * 1. 로그 기록
     * 2. 거리 측정 중지 이벤트 로깅
     */
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

    private void log(Accessory accessory, String str, String str2, String str3) {
        if (accessory.getAlias() == null || accessory.getAlias().isEmpty()) {
            this.mLoggerHelper.log(LoggerHelper.LogEvent.LOG_EVENT_UWB_RANGING_RESULT.toString(), accessory.getName(), accessory.getMac(), str, str2, str3);
        } else {
            this.mLoggerHelper.log(LoggerHelper.LogEvent.LOG_EVENT_UWB_RANGING_RESULT.toString(), accessory.getAlias(), accessory.getMac(), str, str2, str3);
        }
    }
}
