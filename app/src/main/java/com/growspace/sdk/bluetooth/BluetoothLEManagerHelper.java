/**
 * BluetoothLEManagerHelper 클래스는 블루투스 LE(Bluetooth Low Energy) 통신을 관리하는 헬퍼 클래스입니다.
 * 이 클래스는 블루투스 LE 장치의 검색, 연결, 데이터 송수신 등의 기능을 제공합니다.
 * <p>
 * 주요 기능:
 * 1. 블루투스 LE 장치 스캔 및 검색
 * 2. 블루투스 LE 장치 연결 및 연결 해제
 * 3. GATT 서비스 및 특성 검색
 * 4. 데이터 송수신
 * 5. 블루투스 상태 모니터링
 * <p>
 * 사용 방법:
 * 1. BluetoothLEManagerHelper 인스턴스 생성
 * 2. 리스너 등록 (registerListener)
 * 3. 블루투스 LE 장치 스캔 시작 (startLeDeviceScan)
 * 4. 원하는 장치에 연결 (connect)
 * 5. 데이터 송수신 (transmit)
 * 6. 작업 완료 후 리스너 해제 (unregisterListener)
 */
package com.growspace.sdk.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.growspace.sdk.bluetooth.model.BluetoothLERemoteDevice;
import com.growspace.sdk.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BluetoothLEManagerHelper {
    // 로그 태그 - 디버깅 및 로깅에 사용되는 식별자
    private static final String TAG = "BluetoothLEManagerHelper";
    
    // 블루투스 상태 변경 수신기 - 블루투스 상태 변화를 감지하는 BroadcastReceiver
    private final BroadcastReceiver bluetoothStateChangeReceiver;
    
    // 페어링 상태 변경 수신기 - 장치 페어링 상태 변화를 감지하는 BroadcastReceiver
    private final BroadcastReceiver bondStateChangeReceiver;
    
    // 블루투스 어댑터 - 시스템의 블루투스 기능을 제어하는 핵심 객체
    private final BluetoothAdapter mBluetoothAdapter;
    
    // 블루투스 LE 스캐너 - BLE 장치를 검색하는 객체
    private final BluetoothLeScanner mBluetoothLeScanner;
    
    // 블루투스 매니저 - 블루투스 서비스를 관리하는 시스템 서비스
    private BluetoothManager mBluetoothManager;
    
    // 컨텍스트 - Android 애플리케이션 컨텍스트
    private final Context mContext;
    
    // 서비스 UUID - BLE 장치의 서비스 식별자
    private static final UUID serviceUUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    
    // 수신(RX) 특성 UUID - 데이터 수신을 위한 특성 식별자
    private static final UUID rxCharacteristicUUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    
    // 송신(TX) 특성 UUID - 데이터 송신을 위한 특성 식별자
    private static final UUID txCharacteristicUUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    
    // 디스크립터 UUID - 특성의 속성을 정의하는 디스크립터 식별자
    private static final UUID descriptorUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    
    // 리스너 - 블루투스 이벤트를 수신하는 콜백 인터페이스
    private Listener mListener = null;
    
    // 블루투스 LE 원격 장치 목록 - 연결된 BLE 장치들을 관리하는 맵
    // 키: 장치 주소, 값: BluetoothLERemoteDevice 객체
    private final HashMap<String, BluetoothLERemoteDevice> mBluetoothLERemoteDeviceList = new HashMap<>();
    
    // 스캔 콜백 - BLE 장치 스캔 결과를 처리하는 콜백
    private final ScanCallback scanCallback = new ScanCallback() {
        /**
         * 스캔 결과를 처리하는 콜백 메서드
         * 
         * @param i 스캔 결과 코드
         * @param scanResult 스캔 결과 객체, 발견된 BLE 장치의 정보를 포함
         * 
         * 처리 과정:
         * 1. BLUETOOTH_CONNECT 권한 확인
         * 2. 장치 이름과 주소 추출
         * 3. onScan 메서드를 통해 리스너에 결과 전달
         */
        @Override
        public void onScanResult(int i, ScanResult scanResult) {
            if (ActivityCompat.checkSelfPermission(mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
                onScan(scanResult.getDevice().getName(), scanResult.getDevice().getAddress());
            } else {
                Log.e(BluetoothLEManagerHelper.TAG, "Missing required permission to get scanned device info");
            }
        }
    };

    // GATT 콜백 - BLE 장치와의 GATT 통신을 처리하는 콜백
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * 연결 상태 변경 콜백
         * 
         * @param bluetoothGatt GATT 클라이언트 객체
         * @param i 상태 코드 (0: 성공, 그 외: 오류)
         * @param i2 연결 상태 (0: 연결 해제, 2: 연결됨)
         * 
         * 처리 과정:
         * 1. 상태 코드 확인 (오류 발생 시 연결 종료)
         * 2. 연결 상태 확인
         * 3. 연결 성공 시 서비스 검색 시작
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onConnectionStateChange. Status: " + i + " State: " + i2);
            if (i != 0) {
                close(bluetoothGatt.getDevice().getAddress());
                onDisconnect(bluetoothGatt.getDevice().getAddress());
                return;
            }
            if (i2 != 2) {
                if (i2 == 0) {
                    close(bluetoothGatt.getDevice().getAddress());
                    onDisconnect(bluetoothGatt.getDevice().getAddress());
                    return;
                }
                return;
            }
            BluetoothLERemoteDevice bluetoothLERemoteDevice = new BluetoothLERemoteDevice();
            bluetoothLERemoteDevice.setBluetoothGatt(bluetoothGatt);
            mBluetoothLERemoteDeviceList.put(bluetoothGatt.getDevice().getAddress(), bluetoothLERemoteDevice);
            if (discoverServices(bluetoothGatt)) {
                return;
            }
            Log.e(BluetoothLEManagerHelper.TAG, "Failed to start discover services");
            close(bluetoothGatt.getDevice().getAddress());
        }

        /**
         * 서비스 발견 콜백
         * 
         * @param bluetoothGatt GATT 클라이언트 객체
         * @param i 상태 코드 (0: 성공, 그 외: 오류)
         * 
         * 처리 과정:
         * 1. 서비스 검색 상태 확인
         * 2. 특성 검색 및 설정
         * 3. 알림(Notification) 활성화
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onServicesDiscovered status: " + i);
            if (!getCharacteristics(bluetoothGatt)) {
                Log.e(BluetoothLEManagerHelper.TAG, "Failed to start get characteristics");
                close(bluetoothGatt.getDevice().getAddress());
            }
            if (writeDescriptorEnableNotification(bluetoothGatt)) {
                return;
            }
            Log.e(BluetoothLEManagerHelper.TAG, "Failed to start write descriptor to enable notification");
            close(bluetoothGatt.getDevice().getAddress());
        }

        /**
         * 특성 변경 콜백 - 알림(Notification) 수신 시 호출
         * 
         * @param bluetoothGatt GATT 클라이언트 객체
         * @param bluetoothGattCharacteristic 변경된 특성
         * @param data 수신된 데이터
         * 
         * 처리 과정:
         * 1. 수신된 데이터 확인
         * 2. 데이터 처리 및 리스너에 전달
         */
        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt bluetoothGatt, @NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic, @NonNull byte[] data) {
            Log.d(BluetoothLEManagerHelper.TAG, "onCharacteristicChanged");
            if (readCharacteristicData(bluetoothGatt, bluetoothGattCharacteristic, data)) {
                return;
            }
            Log.e(BluetoothLEManagerHelper.TAG, "Failed to start read characteristic data");
            close(bluetoothGatt.getDevice().getAddress());
        }

        /**
         * 디스크립터 쓰기 콜백
         * 
         * @param bluetoothGatt GATT 클라이언트 객체
         * @param bluetoothGattDescriptor 쓰여진 디스크립터
         * @param i 상태 코드 (0: 성공, 그 외: 오류)
         * 
         * 처리 과정:
         * 1. 디스크립터 쓰기 상태 확인
         * 2. 성공 시 MTU 업데이트
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            Log.d(BluetoothLEManagerHelper.TAG, "onDescriptorWrite status: " + i);
            if (i == 0) {
                if (updateMtu(bluetoothGatt)) {
                    return;
                }
                Log.e(BluetoothLEManagerHelper.TAG, "Failed to start update MTU");
                close(bluetoothGatt.getDevice().getAddress());
                return;
            }
            Log.e(BluetoothLEManagerHelper.TAG, "Failed to write descriptor");
            close(bluetoothGatt.getDevice().getAddress());
        }

        /**
         * 특성 쓰기 콜백
         * 
         * @param bluetoothGatt GATT 클라이언트 객체
         * @param bluetoothGattCharacteristic 쓰여진 특성
         * @param i 상태 코드 (0: 성공, 그 외: 오류)
         * 
         * 처리 과정:
         * 1. 쓰기 작업 상태 확인
         * 2. 실패 시 연결 종료
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onCharacteristicWrite. Status: " + i);
            if (i != 0) {
                Log.e(BluetoothLEManagerHelper.TAG, "Failed to write characteristic");
                close(bluetoothGatt.getDevice().getAddress());
            }
        }

        /**
         * 특성 읽기 콜백
         * 
         * @param bluetoothGatt GATT 클라이언트 객체
         * @param bluetoothGattCharacteristic 읽은 특성
         * @param i 상태 코드 (0: 성공, 그 외: 오류)
         * 
         * 처리 과정:
         * 1. 읽기 작업 상태 확인
         * 2. 실패 시 연결 종료
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onCharacteristicRead. Status: " + i);
            if (i != 0) {
                Log.e(BluetoothLEManagerHelper.TAG, "Failed to read characteristic");
                close(bluetoothGatt.getDevice().getAddress());
            }
        }

        /**
         * 디스크립터 읽기 콜백
         * 
         * @param bluetoothGatt GATT 클라이언트 객체
         * @param bluetoothGattDescriptor 읽은 디스크립터
         * @param i 상태 코드 (0: 성공, 그 외: 오류)
         * 
         * 처리 과정:
         * 1. 읽기 작업 상태 확인
         * 2. 실패 시 연결 종료
         */
        @Override
        public void onDescriptorRead(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onCharacteristicRead. Status: " + i);
            if (i != 0) {
                Log.e(BluetoothLEManagerHelper.TAG, "Failed to read descriptor");
                close(bluetoothGatt.getDevice().getAddress());
            }
        }

        /**
         * MTU 변경 콜백
         * 
         * @param bluetoothGatt GATT 클라이언트 객체
         * @param i 새로운 MTU 값
         * @param i2 상태 코드 (0: 성공, 그 외: 오류)
         * 
         * 처리 과정:
         * 1. MTU 변경 상태 확인
         * 2. 성공 시 연결 완료 이벤트 발생
         */
        @Override
        public void onMtuChanged(BluetoothGatt bluetoothGatt, int i, int i2) {
            Log.d(BluetoothLEManagerHelper.TAG, "BluetoothGattCallback onMtuChanged. Status: " + i2);
            if (i2 == 0) {
                if (ActivityCompat.checkSelfPermission(mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
                    onConnect(bluetoothGatt.getDevice().getName(), bluetoothGatt.getDevice().getAddress());
                } else {
                    Log.e(BluetoothLEManagerHelper.TAG, "Missing required permission to get scanned device info");
                }
                return;
            }
            Log.e(BluetoothLEManagerHelper.TAG, "Failed to update MTU");
            close(bluetoothGatt.getDevice().getAddress());
        }
    };

    /**
     * 블루투스 LE 이벤트 리스너 인터페이스
     * 이 인터페이스는 블루투스 LE 관련 이벤트를 수신하기 위한 콜백 메서드들을 정의합니다.
     */
    public interface Listener {
        /**
         * 블루투스 LE 데이터 수신 콜백
         * 
         * @param str 장치 주소 - 데이터를 수신한 BLE 장치의 MAC 주소
         * @param bArr 수신된 데이터 - BLE 장치로부터 수신된 바이트 배열
         * 
         * 호출 시점:
         * - BLE 장치로부터 데이터를 수신했을 때
         * - 알림(Notification)이 활성화된 특성이 변경되었을 때
         */
        void onBluetoothLEDataReceived(String str, byte[] bArr);

        /**
         * 블루투스 LE 장치 페어링 완료 콜백
         * 
         * @param str 장치 이름 - 페어링된 BLE 장치의 이름
         * @param str2 장치 주소 - 페어링된 BLE 장치의 MAC 주소
         * 
         * 호출 시점:
         * - BLE 장치와의 페어링이 성공적으로 완료되었을 때
         */
        void onBluetoothLEDeviceBonded(String str, String str2);

        /**
         * 블루투스 LE 장치 연결 완료 콜백
         * 
         * @param str 장치 이름 - 연결된 BLE 장치의 이름
         * @param str2 장치 주소 - 연결된 BLE 장치의 MAC 주소
         * 
         * 호출 시점:
         * - BLE 장치와의 연결이 성공적으로 완료되었을 때
         * - GATT 연결이 설정되고 MTU가 업데이트된 후
         */
        void onBluetoothLEDeviceConnected(String str, String str2);

        /**
         * 블루투스 LE 장치 연결 해제 콜백
         * 
         * @param str 장치 주소 - 연결이 해제된 BLE 장치의 MAC 주소
         * 
         * 호출 시점:
         * - BLE 장치와의 연결이 해제되었을 때
         * - 연결 오류가 발생했을 때
         * - 명시적으로 연결을 종료했을 때
         */
        void onBluetoothLEDeviceDisconnected(String str);

        /**
         * 블루투스 LE 장치 스캔 결과 콜백
         * 
         * @param str 장치 이름 - 스캔된 BLE 장치의 이름
         * @param str2 장치 주소 - 스캔된 BLE 장치의 MAC 주소
         * 
         * 호출 시점:
         * - BLE 장치 스캔 중 새로운 장치가 발견되었을 때
         */
        void onBluetoothLEDeviceScanned(String str, String str2);

        /**
         * 블루투스 LE 상태 변경 콜백
         * 
         * @param i 상태 코드 - 블루투스 상태 변경 코드
         * 
         * 호출 시점:
         * - 블루투스 상태가 변경되었을 때 (켜짐/꺼짐 등)
         */
        void onBluetoothLEStateChanged(int i);
    }

    /**
     * BluetoothLEManagerHelper 생성자
     * 
     * @param context Android 컨텍스트 - 블루투스 서비스에 접근하기 위한 컨텍스트
     * 
     * 초기화 과정:
     * 1. 블루투스 관련 시스템 서비스 획득
     * 2. 블루투스 어댑터 및 스캐너 초기화
     * 3. 브로드캐스트 리시버 등록
     */
    public BluetoothLEManagerHelper(Context context) {
        // 페어링 상태 변경 수신기 초기화
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context2, Intent intent) {
                if (ActivityCompat.checkSelfPermission(mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
                    String action = intent.getAction();
                    if (action == null || !action.equals("android.bluetooth.device.action.BOND_STATE_CHANGED")) {
                        return;
                    }
                    int intExtra = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", -1);
                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE", BluetoothDevice.class);

                    if (bluetoothDevice != null) {
                        Log.d(BluetoothLEManagerHelper.TAG, "Bluetooth bond state changed to " + intExtra + " for device " + bluetoothDevice.getAddress());
                    } else {
                        Log.e(BluetoothLEManagerHelper.TAG, "Bluetooth bond state changed to " + intExtra + " for device bluetoothDevice Null");
                        return;
                    }

                    if (intExtra != 12) {
                        return;
                    }

                    onBonded(bluetoothDevice.getName(), bluetoothDevice.getAddress());
                } else {
                    Log.e(BluetoothLEManagerHelper.TAG, "Missing required permission to get bonded device info");
                }
            }
        };
        this.bondStateChangeReceiver = broadcastReceiver;
        
        // 블루투스 상태 변경 수신기 초기화
        this.bluetoothStateChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context2, Intent intent) {
                if (Objects.equals(intent.getAction(), "android.bluetooth.adapter.action.STATE_CHANGED")) {
                    int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                    Log.d(BluetoothLEManagerHelper.TAG, "Bluetooth state changed to " + intExtra);
                    onStateChanged(intExtra);
                }
            }
        };
        
        // 블루투스 관련 초기화
        this.mContext = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothManager = bluetoothManager;
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        this.mBluetoothAdapter = adapter;
        this.mBluetoothLeScanner = adapter.getBluetoothLeScanner();
        this.mContext.registerReceiver(broadcastReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
    }

    /**
     * 리스너 등록 메서드
     * 
     * @param listener 등록할 리스너 - 블루투스 이벤트를 수신할 리스너 객체
     * 
     * 처리 과정:
     * 1. 리스너 객체 저장
     * 2. 블루투스 상태 변경 브로드캐스트 리시버 등록
     */
    public void registerListener(Listener listener) {
        this.mListener = listener;
        this.mContext.registerReceiver(this.bluetoothStateChangeReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
    }

    /**
     * 리스너 해제 메서드
     * 
     * 처리 과정:
     * 1. 리스너 객체 제거
     * 2. 블루투스 상태 변경 브로드캐스트 리시버 해제
     */
    public void unregisterListener() {
        this.mListener = null;
        this.mContext.unregisterReceiver(this.bluetoothStateChangeReceiver);
    }

    /**
     * 블루투스 LE 지원 여부 확인 메서드
     * 
     * @return 지원 여부 - 블루투스 LE가 지원되는지 여부
     * 
     * 확인 과정:
     * 1. 블루투스 어댑터 존재 여부 확인
     */
    public boolean isSupported() {
        return this.mBluetoothAdapter != null;
    }

    /**
     * 블루투스 활성화 여부 확인 메서드
     * 
     * @return 활성화 여부 - 블루투스가 활성화되어 있는지 여부
     * 
     * 확인 과정:
     * 1. 블루투스 어댑터 존재 여부 확인
     * 2. 블루투스 활성화 상태 확인
     */
    public boolean isEnabled() {
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * 장치 페어링 여부 확인 메서드
     * 
     * @param str 확인할 장치의 MAC 주소
     * @return 페어링 여부 - 해당 장치가 이미 페어링되어 있는지 여부
     * 
     * 확인 과정:
     * 1. BLUETOOTH_CONNECT 권한 확인
     * 2. 페어링된 장치 목록에서 해당 주소 검색
     */
    public boolean isBondedDevice(String str) {
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            for (BluetoothDevice bluetoothDevice : this.mBluetoothAdapter.getBondedDevices()) {
                if (str.equals(bluetoothDevice.getAddress())) {
                    return true;
                }
            }
            return false;
        }
        Log.e(TAG, "Missing required permission to scan for BLE devices");
        return false;
    }

    /**
     * BLE 장치 스캔 시작 메서드
     * 
     * @return 성공 여부 - 스캔이 성공적으로 시작되었는지 여부
     * 
     * 처리 과정:
     * 1. 블루투스 LE 스캐너 초기화 확인
     * 2. 스캔 필터 설정 (서비스 UUID 기반)
     * 3. 스캔 설정 구성 (스캔 모드, 지연 시간 등)
     * 4. BLUETOOTH_SCAN 권한 확인
     * 5. 스캔 시작
     */
    public boolean startLeDeviceScan() {
        String str = TAG;
        Log.d(str, "Bluetooth starting LE Scanning");
        if (this.mBluetoothLeScanner == null) {
            Log.e(str, "BluetoothLeScanner not initialized");
            return false;
        }
        ArrayList<ScanFilter> arrayList = new ArrayList<>();
        arrayList.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(serviceUUID)).build());
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(1);
        builder.setReportDelay(0L);
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_SCAN") == 0) {
            Log.d(str, "Bluetooth SCAN successfully started");
            this.mBluetoothLeScanner.startScan(arrayList, builder.build(), this.scanCallback);
            return true;
        }
        Log.e(str, "Missing required permission to scan for BLE devices");
        return false;
    }

    /**
     * BLE 장치 스캔 중지 메서드
     * 
     * @return 성공 여부 - 스캔이 성공적으로 중지되었는지 여부
     * 
     * 처리 과정:
     * 1. 블루투스 LE 스캐너 초기화 확인
     * 2. BLUETOOTH_SCAN 권한 확인
     * 3. 대기 중인 스캔 결과 처리
     * 4. 스캔 중지
     */
    public boolean stopLeDeviceScan() {
        String str = TAG;
        Log.d(str, "Bluetooth stopping LE Scanning");
        if (this.mBluetoothLeScanner == null) {
            Log.e(str, "BluetoothLeScanner not initialized");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_SCAN") == 0) {
            Log.d(str, "Bluetooth SCAN successfully stopped");
            this.mBluetoothLeScanner.flushPendingScanResults(this.scanCallback);
            this.mBluetoothLeScanner.stopScan(this.scanCallback);
            return true;
        }
        Log.e(str, "Missing required permission to scan for BLE devices");
        return false;
    }

    /**
     * BLE 장치 연결 메서드
     * 
     * @param str 연결할 장치의 MAC 주소
     * @return 성공 여부 - 연결 시도가 성공적으로 시작되었는지 여부
     * 
     * 처리 과정:
     * 1. 블루투스 어댑터 및 주소 유효성 확인
     * 2. 원격 장치 객체 획득
     * 3. 기존 연결 종료
     * 4. BLUETOOTH_CONNECT 권한 확인
     * 5. GATT 연결 시작
     */
    public boolean connect(String str) {
        String str2 = TAG;
        Log.d(str2, "Proceed to connect to device: " + str);
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter == null || str == null) {
            Log.e(str2, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(str);
        if (remoteDevice == null) {
            Log.e(str2, "Device not found. Unable to connect.");
            return false;
        }
        close(str);
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            remoteDevice.connectGatt(this.mContext, false, this.mGattCallback);
            return true;
        }
        Log.e(str2, "Missing required permission to connect to device");
        return false;
    }

    /**
     * 데이터 송신 메서드
     * 
     * @param str 대상 장치의 MAC 주소
     * @param bArr 송신할 데이터 바이트 배열
     * @return 성공 여부 - 데이터 송신이 성공적으로 시작되었는지 여부
     * 
     * 처리 과정:
     * 1. 장치 및 특성 유효성 확인
     * 2. BLUETOOTH_CONNECT 권한 확인
     * 3. 특성 값 설정 및 쓰기 타입 지정
     * 4. 특성 쓰기 요청
     */
    public boolean transmit(String str, byte[] bArr) {
        String str2 = TAG;
        Log.d(str2, "Proceed to transmit to " + str + " data: " + Utils.byteArrayToHexString(bArr));
        BluetoothLERemoteDevice bluetoothLERemoteDevice = this.mBluetoothLERemoteDeviceList.get(str);
        if (bluetoothLERemoteDevice == null || bluetoothLERemoteDevice.getBluetoothGatt() == null || bluetoothLERemoteDevice.getRxCharacteristic() == null) {
            Log.e(str2, "BluetoothGatt not initialized or uninitialized characteristic for this device.");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            bluetoothLERemoteDevice.getRxCharacteristic().setValue(bArr);
            bluetoothLERemoteDevice.getRxCharacteristic().setWriteType(2);
            return bluetoothLERemoteDevice.getBluetoothGatt().writeCharacteristic(bluetoothLERemoteDevice.getRxCharacteristic());
        }
        Log.e(str2, "Missing required permission to write characteristic");
        return false;
    }

    /**
     * 연결 종료 메서드
     * 
     * @param str 종료할 장치의 MAC 주소
     * @return 성공 여부 - 연결이 성공적으로 종료되었는지 여부
     * 
     * 처리 과정:
     * 1. 장치 및 GATT 클라이언트 유효성 확인
     * 2. BLUETOOTH_CONNECT 권한 확인
     * 3. GATT 연결 종료
     * 4. 장치 목록에서 제거
     */
    public boolean close(String str) {
        String str2 = TAG;
        Log.d(str2, "Proceed to close connection with device " + str);
        BluetoothLERemoteDevice bluetoothLERemoteDevice = this.mBluetoothLERemoteDeviceList.get(str);
        if (bluetoothLERemoteDevice == null || bluetoothLERemoteDevice.getBluetoothGatt() == null) {
            Log.e(str2, "BluetoothGatt not initialized or uninitialized characteristic for this device.");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            bluetoothLERemoteDevice.getBluetoothGatt().close();
            this.mBluetoothLERemoteDeviceList.remove(str);
            return true;
        }
        Log.e(str2, "Missing required permission to close connection");
        return false;
    }

    /**
     * 서비스 검색 메서드
     * 
     * @param bluetoothGatt GATT 클라이언트 객체
     * @return 성공 여부 - 서비스 검색이 성공적으로 시작되었는지 여부
     * 
     * 처리 과정:
     * 1. GATT 클라이언트 유효성 확인
     * 2. BLUETOOTH_CONNECT 권한 확인
     * 3. 서비스 검색 시작
     */
    public boolean discoverServices(BluetoothGatt bluetoothGatt) {
        String str = TAG;
        Log.d(str, "Proceed to discover services");
        if (bluetoothGatt == null) {
            Log.e(str, "BluetoothGatt not initialized.");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            return bluetoothGatt.discoverServices();
        }
        Log.e(str, "Missing required permission to discover services");
        return false;
    }

    /**
     * 특성 검색 및 설정 메서드
     * 
     * @param bluetoothGatt GATT 클라이언트 객체
     * @return 성공 여부 - 특성이 성공적으로 검색되고 설정되었는지 여부
     * 
     * 처리 과정:
     * 1. GATT 클라이언트 유효성 확인
     * 2. 서비스 검색
     * 3. RX/TX 특성 검색 및 설정
     */
    public boolean getCharacteristics(BluetoothGatt bluetoothGatt) {
        String str = TAG;
        Log.d(str, "Proceed to get characteristics");
        if (bluetoothGatt == null) {
            Log.e(str, "BluetoothGatt not initialized.");
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(serviceUUID);
        if (service == null) {
            Log.e(str, "Service not found");
            return false;
        }
        BluetoothLERemoteDevice bluetoothLERemoteDevice = this.mBluetoothLERemoteDeviceList.get(bluetoothGatt.getDevice().getAddress());
        if (bluetoothLERemoteDevice == null) {
            Log.e(str, "BluetoothLERemoteDevice not found.");
            return false;
        }
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        for (int i = 0; i < characteristics.size(); i++) {
            BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristics.get(i);
            if (bluetoothGattCharacteristic.getUuid().equals(rxCharacteristicUUID)) {
                Log.i(TAG, "Write characteristic found, UUID is: " + bluetoothGattCharacteristic.getUuid().toString());
                bluetoothLERemoteDevice.setRxCharacteristic(bluetoothGattCharacteristic);
            } else if (bluetoothGattCharacteristic.getUuid().equals(txCharacteristicUUID)) {
                Log.i(TAG, "Notify characteristic found, UUID is " + bluetoothGattCharacteristic.getUuid().toString());
                bluetoothLERemoteDevice.setTxCharacteristic(bluetoothGattCharacteristic);
            }
        }
        return bluetoothLERemoteDevice.getRxCharacteristic() != null && bluetoothLERemoteDevice.getTxCharacteristic() != null;
    }

    /**
     * 알림 활성화를 위한 디스크립터 쓰기 메서드
     * 
     * @param bluetoothGatt GATT 클라이언트 객체
     * @return 성공 여부 - 디스크립터가 성공적으로 쓰여졌는지 여부
     * 
     * 처리 과정:
     * 1. GATT 클라이언트 유효성 확인
     * 2. BLUETOOTH_CONNECT 권한 확인
     * 3. 특성 알림 설정
     * 4. 디스크립터 값 설정 및 쓰기
     */
    public boolean writeDescriptorEnableNotification(BluetoothGatt bluetoothGatt) {
        String str = TAG;
        Log.d(str, "Proceed to write descriptor to enable notification");
        if (bluetoothGatt == null) {
            Log.e(str, "BluetoothGatt not initialized.");
            return false;
        }
        BluetoothLERemoteDevice bluetoothLERemoteDevice = this.mBluetoothLERemoteDeviceList.get(bluetoothGatt.getDevice().getAddress());
        if (bluetoothLERemoteDevice == null) {
            Log.e(str, "BluetoothLERemoteDevice not found.");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            if (!bluetoothGatt.setCharacteristicNotification(bluetoothLERemoteDevice.getTxCharacteristic(), true)) {
                Log.e(str, "Failed setCharacteristicNotification txCharacteristic");
                return false;
            }
            BluetoothGattDescriptor descriptor = bluetoothLERemoteDevice.getTxCharacteristic().getDescriptor(descriptorUUID);
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                return bluetoothGatt.writeDescriptor(descriptor);
            }
            Log.e(str, "descriptor is null");
            return false;
        }
        Log.e(str, "Missing required permission to write descriptor to enable notification");
        return false;
    }

    /**
     * MTU 업데이트 메서드
     * 
     * @param bluetoothGatt GATT 클라이언트 객체
     * @return 성공 여부 - MTU 업데이트가 성공적으로 시작되었는지 여부
     * 
     * 처리 과정:
     * 1. GATT 클라이언트 유효성 확인
     * 2. BLUETOOTH_CONNECT 권한 확인
     * 3. MTU 업데이트 요청 (247 바이트)
     */
    public boolean updateMtu(BluetoothGatt bluetoothGatt) {
        String str = TAG;
        Log.d(str, "Proceed to update MTU");
        if (bluetoothGatt == null) {
            Log.e(str, "BluetoothGatt not initialized.");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this.mContext, "android.permission.BLUETOOTH_CONNECT") == 0) {
            return bluetoothGatt.requestMtu(247);
        }
        Log.e(str, "Missing required permission to update MTU");
        return false;
    }

    /**
     * 특성 데이터 읽기 메서드
     * 
     * @param bluetoothGatt GATT 클라이언트 객체
     * @param bluetoothGattCharacteristic 읽을 특성
     * @param value 수신된 데이터
     * @return 성공 여부 - 데이터가 성공적으로 처리되었는지 여부
     * 
     * 처리 과정:
     * 1. GATT 클라이언트 및 특성 유효성 확인
     * 2. 데이터 유효성 확인
     * 3. 데이터 로깅
     * 4. 리스너에 데이터 전달
     */
    public boolean readCharacteristicData(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] value) {
        if (bluetoothGatt == null || bluetoothGattCharacteristic == null) {
            Log.e(TAG, "BluetoothGatt or BluetoothGattCharacteristic not initialized");
            return false;
        }
        if (value == null || value.length == 0) {
            return false;
        }
        Log.d(TAG, "Bluetooth LE Device " + bluetoothGatt.getDevice().getAddress() + " Data received: " + Utils.byteArrayToHexString(value));
        onDataReceived(bluetoothGatt.getDevice().getAddress(), value);
        return true;
    }

    /**
     * 페어링 완료 이벤트 처리 메서드
     * 
     * @param str 장치 이름 - 페어링된 장치의 이름
     * @param str2 장치 주소 - 페어링된 장치의 MAC 주소
     * 
     * 처리 과정:
     * 1. 메인 스레드에서 리스너 콜백 호출
     */
    public void onBonded(final String str, final String str2) {
        new Handler(Looper.getMainLooper()).post(() -> listenerOnBluetoothLEDeviceBonded(str, str2));
    }

    /**
     * 페어링 완료 리스너 콜백 호출 메서드
     * 
     * @param str 장치 이름
     * @param str2 장치 주소
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너의 onBluetoothLEDeviceBonded 콜백 호출
     */
    void listenerOnBluetoothLEDeviceBonded(String str, String str2) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEDeviceBonded(str, str2);
        }
    }

    /**
     * 스캔 결과 이벤트 처리 메서드
     * 
     * @param str 장치 이름 - 스캔된 장치의 이름
     * @param str2 장치 주소 - 스캔된 장치의 MAC 주소
     * 
     * 처리 과정:
     * 1. 메인 스레드에서 리스너 콜백 호출
     */
    public void onScan(final String str, final String str2) {
        new Handler(Looper.getMainLooper()).post(() -> listenerOnBluetoothLEDeviceScanned(str, str2));
    }

    /**
     * 스캔 결과 리스너 콜백 호출 메서드
     * 
     * @param str 장치 이름
     * @param str2 장치 주소
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너의 onBluetoothLEDeviceScanned 콜백 호출
     */
    void listenerOnBluetoothLEDeviceScanned(String str, String str2) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEDeviceScanned(str, str2);
        }
    }

    /**
     * 연결 완료 이벤트 처리 메서드
     * 
     * @param str 장치 이름 - 연결된 장치의 이름
     * @param str2 장치 주소 - 연결된 장치의 MAC 주소
     * 
     * 처리 과정:
     * 1. 메인 스레드에서 리스너 콜백 호출
     */
    public void onConnect(final String str, final String str2) {
        new Handler(Looper.getMainLooper()).post(() -> listenerOnBluetoothLEDeviceConnected(str, str2));
    }

    /**
     * 연결 완료 리스너 콜백 호출 메서드
     * 
     * @param str 장치 이름
     * @param str2 장치 주소
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너의 onBluetoothLEDeviceConnected 콜백 호출
     */
    void listenerOnBluetoothLEDeviceConnected(String str, String str2) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEDeviceConnected(str, str2);
        }
    }

    /**
     * 연결 해제 이벤트 처리 메서드
     * 
     * @param str 장치 주소 - 연결이 해제된 장치의 MAC 주소
     * 
     * 처리 과정:
     * 1. 메인 스레드에서 리스너 콜백 호출
     */
    public void onDisconnect(final String str) {
        new Handler(Looper.getMainLooper()).post(() -> listenerOnBluetoothLEDeviceDisconnected(str));
    }

    /**
     * 연결 해제 리스너 콜백 호출 메서드
     * 
     * @param str 장치 주소
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너의 onBluetoothLEDeviceDisconnected 콜백 호출
     */
    void listenerOnBluetoothLEDeviceDisconnected(String str) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEDeviceDisconnected(str);
        }
    }

    /**
     * 데이터 수신 이벤트 처리 메서드
     * 
     * @param str 장치 주소 - 데이터를 수신한 장치의 MAC 주소
     * @param bArr 수신된 데이터 - 바이트 배열 형태의 데이터
     * 
     * 처리 과정:
     * 1. 메인 스레드에서 리스너 콜백 호출
     */
    private void onDataReceived(final String str, final byte[] bArr) {
        new Handler(Looper.getMainLooper()).post(() -> listenerOnBluetoothLEDataReceived(str, bArr));
    }

    /**
     * 데이터 수신 리스너 콜백 호출 메서드
     * 
     * @param str 장치 주소
     * @param bArr 수신된 데이터
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너의 onBluetoothLEDataReceived 콜백 호출
     */
    void listenerOnBluetoothLEDataReceived(String str, byte[] bArr) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEDataReceived(str, bArr);
        }
    }

    /**
     * 블루투스 상태 변경 이벤트 처리 메서드
     * 
     * @param i 상태 코드 - 블루투스의 새로운 상태 코드
     * 
     * 처리 과정:
     * 1. 메인 스레드에서 리스너 콜백 호출
     */
    public void onStateChanged(final int i) {
        new Handler(Looper.getMainLooper()).post(() -> listenerOnBluetoothLEStateChanged(i));
    }

    /**
     * 블루투스 상태 변경 리스너 콜백 호출 메서드
     * 
     * @param i 상태 코드
     * 
     * 처리 과정:
     * 1. 리스너 존재 여부 확인
     * 2. 리스너의 onBluetoothLEStateChanged 콜백 호출
     */
    void listenerOnBluetoothLEStateChanged(int i) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onBluetoothLEStateChanged(i);
        }
    }
}
