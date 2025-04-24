/**
 * UwbManagerHelper 클래스는 UWB(Ultra-Wideband) 통신을 관리하는 헬퍼 클래스입니다.
 * 이 클래스는 UWB 장치의 검색, 연결, 거리 측정 등의 기능을 제공합니다.
 * <p>
 * 주요 기능:
 * 1. UWB 장치 연결 및 연결 해제
 * 2. 거리 측정 시작 및 중지
 * 3. UWB 프로파일 및 역할 관리
 * 4. 거리 측정 결과 처리
 * <p>
 * 사용 방법:
 * 1. UwbManagerHelper 인스턴스 생성
 * 2. 리스너 등록 (registerListener)
 * 3. 거리 측정 시작 (startRanging)
 * 4. 거리 측정 결과 수신 (onRangingResult)
 * 5. 작업 완료 후 리스너 해제 (unregisterListener)
 */
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
    
    /**
     * 로그 태그 - 디버깅 및 로깅에 사용되는 식별자
     */
    private static final String TAG = "UwbManagerHelper";
    
    /**
     * UWB 역할 매핑 - 역할 문자열을 숫자로 매핑
     */
    private static final Map<String, Integer> uwbRoleMap;
    
    /**
     * 컨텍스트 - Android 애플리케이션 컨텍스트
     */
    private final Context mContext;
    
    /**
     * UWB 매니저 - UWB 서비스를 관리하는 핵심 객체
     */
    private UwbManager mUwbManager;
    
    /**
     * 현재 UWB 채널 - 현재 사용 중인 통신 채널
     */
    private int mUwbChannel = 9;
    
    /**
     * 현재 UWB 프리앰블 인덱스 - 현재 사용 중인 프리앰블 식별자
     */
    private int mUwbPreambleIndex = 10;
    
    /**
     * 선호 UWB 프로파일 ID - 선호하는 프로파일 식별자
     */
    private int mPreferredUwbProfileId = 1;
    
    /**
     * 선호 UWB 폰 역할 - 선호하는 폰 역할
     */
    private int mPreferredUwbPhoneRole = 1;
    
    /**
     * 리스너 - UWB 이벤트를 수신하는 콜백 인터페이스
     */
    private Listener mListener = null;
    
    /**
     * UWB 원격 장치 목록 - 연결된 UWB 장치들을 관리하는 맵
     * 키: 장치 주소, 값: UwbRemoteDevice 객체
     */
    private final HashMap<String, UwbRemoteDevice> mUwbRemoteDeviceList = new HashMap<>();

    /**
     * UWB 이벤트 리스너 인터페이스
     * 이 인터페이스는 UWB 관련 이벤트를 수신하기 위한 콜백 메서드들을 정의합니다.
     */
    public interface Listener {
        /**
         * 거리 측정 기능 지원 여부 콜백
         * 
         * @param rangingCapabilities 거리 측정 기능 정보
         * 호출 시점:
         * - 거리 측정 기능 지원 여부를 확인할 때
         */
        void onRangingCapabilities(RangingCapabilities rangingCapabilities);

        /**
         * 거리 측정 완료 콜백
         * 호출 시점:
         * - 거리 측정이 완료되었을 때
         */
        void onRangingComplete();

        /**
         * 거리 측정 오류 콜백
         * 
         * @param th 발생한 오류
         * 호출 시점:
         * - 거리 측정 중 오류가 발생했을 때
         */
        void onRangingError(Throwable th);

        /**
         * 거리 측정 결과 콜백
         * 
         * @param str 장치 주소 - 거리를 측정한 장치의 주소
         * @param rangingResult 거리 측정 결과
         * 호출 시점:
         * - 새로운 거리 측정 결과가 도착했을 때
         */
        void onRangingResult(String str, RangingResult rangingResult);

        /**
         * 거리 측정 시작 콜백
         * 
         * @param str 장치 주소 - 거리 측정을 시작한 장치의 주소
         * @param uwbPhoneConfigData 폰 설정 데이터 - UWB 통신 설정 정보
         * 호출 시점:
         * - 거리 측정이 시작되었을 때
         */
        void onRangingStarted(String str, UwbPhoneConfigData uwbPhoneConfigData);
    }

    /**
     * UWB 지원 여부 확인 메서드
     * 
     * @return 지원 여부 - UWB가 지원되는지 여부
     */
    public boolean isEnabled() {
        return true;
    }

    static {
        HashMap<String, Integer> hashMap = new HashMap<>();
        uwbRoleMap = hashMap;
        hashMap.put("Controller", 0);
        hashMap.put("Controlee", 1);
    }

    /**
     * UwbManagerHelper 생성자
     * 
     * @param context Android 컨텍스트 - UWB 서비스에 접근하기 위한 컨텍스트
     * 초기화 과정:
     * 1. UWB 매니저 초기화
     * 2. UWB 하드웨어 지원 여부 확인
     */
    public UwbManagerHelper(Context context) {
        this.mUwbManager = null;
        this.mContext = context;
        if (context.getPackageManager().hasSystemFeature("android.hardware.uwb")) {
            this.mUwbManager = UwbManager.createInstance(context);
        }
    }

    /**
     * 리스너 등록 메서드
     * 
     * @param listener 등록할 리스너 - UWB 이벤트를 수신할 리스너 객체
     */
    public void registerListener(Listener listener) {
        this.mListener = listener;
    }

    /**
     * 리스너 해제 메서드
     */
    public void unregisterListener() {
        this.mListener = null;
    }

    /**
     * UWB 지원 여부 확인 메서드
     * 
     * @return 지원 여부 - UWB가 지원되는지 여부
     */
    public boolean isSupported() {
        return this.mUwbManager != null;
    }

    /**
     * UWB 채널 설정 메서드
     * 
     * @param i 설정할 채널 번호
     */
    public void setUwbChannel(int i) {
        this.mUwbChannel = i;
    }

    /**
     * UWB 프리앰블 인덱스 설정 메서드
     * 
     * @param i 설정할 프리앰블 인덱스
     */
    public void setUwbPreambleIndex(int i) {
        this.mUwbPreambleIndex = i;
    }

    /**
     * 선호 UWB 역할 설정 메서드
     * 
     * @param str 설정할 역할 문자열 ("Controller" 또는 "Controlee")
     */
    public void setPreferredUwbRole(String str) {
        Integer num = uwbRoleMap.get(str);
        if (num != null) {
            this.mPreferredUwbPhoneRole = num;
        }
    }

    /**
     * 선호 UWB 프로파일 ID 설정 메서드
     * 
     * @param i 설정할 프로파일 ID
     */
    public void setPreferredUwbProfileId(int i) {
        this.mPreferredUwbProfileId = i;
    }

    /**
     * 거리 측정 시작 메서드
     * 
     * @param str 원격 장치 주소
     * @param uwbDeviceConfigData 장치 설정 데이터
     * @return 성공 여부 - 거리 측정이 성공적으로 시작되었는지 여부
     * 처리 과정:
     * 1. UWB 매니저 및 매개변수 유효성 확인
     * 2. UWB_RANGING 권한 확인
     * 3. 별도 스레드에서 거리 측정 시작
     */
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

    /**
     * 거리 측정 시작 처리 메서드
     * 
     * @param uwbDeviceConfigData 장치 설정 데이터
     * @param str 원격 장치 주소
     * 처리 과정:
     * 1. UWB 장치 역할 선택
     * 2. UWB 프로파일 ID 선택
     * 3. 세션 범위 설정 (Controller/Controlee)
     * 4. 거리 측정 파라미터 구성
     * 5. Flowable 구독 및 결과 처리
     * 세부 처리:
     * - 로컬 주소 및 복합 채널 설정
     * - 세션 ID 생성
     * - 원격 장치 목록 구성
     * - 거리 측정 파라미터 설정
     * - Flowable 구독 및 결과 처리
     * - 폰 설정 데이터 구성 및 콜백 호출
     */
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

    /**
     * 거리 측정 중지 메서드
     * 
     * @param str 원격 장치 주소
     * @return 성공 여부 - 거리 측정이 성공적으로 중지되었는지 여부
     * 처리 과정:
     * 1. 장치 주소로 UWB 원격 장치 조회
     * 2. Disposable 해제
     * 3. 장치 목록에서 제거
     * 예외 처리:
     * - UWB 세션이 시작되지 않은 경우
     * - Disposable이 초기화되지 않은 경우
     * - 예외 발생 시 로그 기록
     */
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

    /**
     * 연결 종료 메서드
     * 
     * @param str 원격 장치 주소
     * @return 성공 여부 - 연결이 성공적으로 종료되었는지 여부
     * 처리 과정:
     * 1. 장치 주소로 UWB 원격 장치 조회
     * 2. Disposable 해제
     * 3. 장치 목록에서 제거
     * 예외 처리:
     * - UWB 세션이 시작되지 않은 경우
     * - Disposable이 초기화되지 않은 경우
     * - 예외 발생 시 로그 기록
     */
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

    /**
     * 거리 측정 기능 지원 여부 확인 메서드
     * 
     * @return 성공 여부 - 거리 측정 기능 지원 여부 확인이 성공적으로 시작되었는지 여부
     * 처리 과정:
     * 1. UWB 매니저 유효성 확인
     * 2. 별도 스레드에서 거리 측정 기능 확인
     * 예외 처리:
     * - UWB 매니저가 사용 불가능한 경우
     */
    public boolean getRangingCapabilities() {
        if (this.mUwbManager == null) {
            Log.e(TAG, "UWB Manager is not available in this device");
            return false;
        }
        new Thread(UwbManagerHelper.this::onRangingCapabilities).start();
        return true;
    }

    /**
     * 거리 측정 기능 확인 처리 메서드
     * 처리 과정:
     * 1. Controlee 세션 범위 획득
     * 2. 거리 측정 기능 정보 조회
     * 3. 결과 콜백 호출
     * 예외 처리:
     * - 세션 범위 획득 실패 시
     * - 예외 발생 시 null 결과 전달
     */
    void onRangingCapabilities() {
        try {
            onRangingCapabilities(UwbManagerRx.controleeSessionScopeSingle(this.mUwbManager).blockingGet().getRangingCapabilities());
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting UWB Ranging capabilities: " + e.getMessage());
            onRangingCapabilities(null);
        }
    }

    /**
     * UWB 프로파일 ID 선택 메서드
     * 
     * @param i 지원되는 프로파일 ID 비트맵
     * @return 선택된 프로파일 ID
     * 선택 우선순위:
     * 1. 선호 프로파일 ID
     * 2. 프로파일 ID 1
     * 3. 프로파일 ID 0
     * 처리 과정:
     * 1. 선호 프로파일 ID 지원 여부 확인
     * 2. 프로파일 ID 1 지원 여부 확인
     * 3. 기본값으로 프로파일 ID 0 반환
     */
    private byte selectUwbProfileId(int i) {
        if (BigInteger.valueOf(i).testBit(this.mPreferredUwbProfileId)) {
            return (byte) this.mPreferredUwbProfileId;
        }
        return BigInteger.valueOf(i).testBit(1) ? (byte) 1 : (byte) 0;
    }

    /**
     * UWB 장치 역할 선택 메서드
     * 
     * @param i 지원되는 역할 비트맵
     * @return 선택된 역할
     * 선택 우선순위:
     * 1. 선호 역할
     * 2. Controlee 역할
     * 3. Controller 역할
     * 처리 과정:
     * 1. 선호 역할 지원 여부 확인
     * 2. Controlee 역할 지원 여부 확인
     * 3. Controller 역할 지원 여부 확인
     */
    private byte selectUwbDeviceRangingRole(int i) {
        int i2 = this.mPreferredUwbPhoneRole;
        if (i2 != 0 || ((i >> 1) & 1) == 0) {
            return ((i2 != 1 || ((i) & 1) == 0) && ((i) & 1) == 0 && ((i >> 1) & 1) != 0) ? (byte) 1 : (byte) 0;
        }
        return (byte) 1;
    }

    /**
     * UWB 장치에서 주소 조회 메서드
     * 
     * @param uwbDevice UWB 장치 객체
     * @return 장치 주소 - 일치하는 장치가 있으면 주소, 없으면 null
     * 처리 과정:
     * 1. 원격 장치 목록 순회
     * 2. 주소 일치 여부 확인
     * 비교 방식:
     * - 장치 주소 문자열 비교
     */
    public String getAddressFromUwbDevice(UwbDevice uwbDevice) {
        for (Map.Entry<String, UwbRemoteDevice> entry : this.mUwbRemoteDeviceList.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue().getUwbDevice().getAddress().toString().equals(uwbDevice.getAddress().toString())) {
                return key;
            }
        }
        return null;
    }

    /**
     * 거리 측정 시작 콜백 호출 메서드
     * 
     * @param str 장치 주소
     * @param uwbPhoneConfigData 폰 설정 데이터
     * 처리 과정:
     * 1. 메인 스레드에서 콜백 호출
     * 2. Handler를 사용한 비동기 처리
     */
    private void onRangingStarted(final String str, final UwbPhoneConfigData uwbPhoneConfigData) {
        new Handler(Looper.getMainLooper()).post(() -> UwbManagerHelper.this.listenerOnRangingStarted(str, uwbPhoneConfigData));
    }

    /**
     * 거리 측정 시작 리스너 호출 메서드
     * 
     * @param str 장치 주소
     * @param uwbPhoneConfigData 폰 설정 데이터
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너가 존재하면 콜백 호출
     */
    void listenerOnRangingStarted(String str, UwbPhoneConfigData uwbPhoneConfigData) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onRangingStarted(str, uwbPhoneConfigData);
        }
    }

    /**
     * 거리 측정 결과 콜백 호출 메서드
     * 
     * @param str 장치 주소
     * @param rangingResult 거리 측정 결과
     * 
     * 처리 과정:
     * 1. 메인 스레드에서 콜백 호출
     * 2. Handler를 사용한 비동기 처리
     */
    public void onRangingResult(final String str, final RangingResult rangingResult) {
        new Handler(Looper.getMainLooper()).post(() -> UwbManagerHelper.this.listenerOnRangingResult(str, rangingResult));
    }

    /**
     * 거리 측정 결과 리스너 호출 메서드
     * 
     * @param str 장치 주소
     * @param rangingResult 거리 측정 결과
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너가 존재하면 콜백 호출
     */
    void listenerOnRangingResult(String str, RangingResult rangingResult) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onRangingResult(str, rangingResult);
        }
    }

    /**
     * 거리 측정 오류 콜백 호출 메서드
     * 
     * @param th 발생한 오류
     * 
     * 처리 과정:
     * 1. 메인 스레드에서 콜백 호출
     * 2. Handler를 사용한 비동기 처리
     * 
     * 비동기 처리:
     * - 메인 스레드의 Looper를 사용하여 Handler 생성
     * - Runnable을 통해 리스너 호출
     */
    public void onRangingError(final Throwable th) {
        new Handler(Looper.getMainLooper()).post(() -> UwbManagerHelper.this.listenerOnRangingError(th));
    }

    /**
     * 거리 측정 오류 리스너 호출 메서드
     * 
     * @param th 발생한 오류
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너가 존재하면 콜백 호출
     * 
     * 안전성:
     * - null 체크를 통한 안전한 리스너 호출
     */
    void listenerOnRangingError(Throwable th) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onRangingError(th);
        }
    }

    /**
     * 거리 측정 완료 콜백 호출 메서드
     * 
     * 처리 과정:
     * 1. 메인 스레드에서 콜백 호출
     * 2. Handler를 사용한 비동기 처리
     * 
     * 비동기 처리:
     * - 메인 스레드의 Looper를 사용하여 Handler 생성
     * - Runnable을 통해 리스너 호출
     */
    public void onRangingComplete() {
        new Handler(Looper.getMainLooper()).post(UwbManagerHelper.this::listenerOnRangingComplete);
    }

    /**
     * 거리 측정 완료 리스너 호출 메서드
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너가 존재하면 콜백 호출
     * 
     * 안전성:
     * - null 체크를 통한 안전한 리스너 호출
     */
    void listenerOnRangingComplete() {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onRangingComplete();
        }
    }

    /**
     * 거리 측정 기능 확인 콜백 호출 메서드
     * 
     * @param rangingCapabilities 거리 측정 기능 정보
     * 
     * 처리 과정:
     * 1. 메인 스레드에서 콜백 호출
     * 2. Handler를 사용한 비동기 처리
     * 
     * 비동기 처리:
     * - 메인 스레드의 Looper를 사용하여 Handler 생성
     * - Runnable을 통해 리스너 호출
     */
    private void onRangingCapabilities(final RangingCapabilities rangingCapabilities) {
        new Handler(Looper.getMainLooper()).post(() -> UwbManagerHelper.this.listenerOnRangingCapabilities(rangingCapabilities));
    }

    /**
     * 거리 측정 기능 확인 리스너 호출 메서드
     * 
     * @param rangingCapabilities 거리 측정 기능 정보
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너가 존재하면 콜백 호출
     * 
     * 안전성:
     * - null 체크를 통한 안전한 리스너 호출
     */
    void listenerOnRangingCapabilities(RangingCapabilities rangingCapabilities) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onRangingCapabilities(rangingCapabilities);
        }
    }
}
