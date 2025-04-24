package com.growspace.sdk.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import java.util.Objects;

public class LocationManagerHelper {
    // 시스템 서비스로부터 위치 제공자(GPS, network) 상태를 조회할 수 있는 매니저 객체
    private final LocationManager locationManager;
    // 브로드캐스트 수신자 등록/해제 시에 사용할 Context
    private final Context mContext;
    // 외부에서 상태 변경 콜백을 받을 리스너를 저장
    private Listener mListener = null;

    // 위치 모드 변경(Intent.ACTION_MODE_CHANGED)을 수신하기 위한 BroadcastReceiver
    private final BroadcastReceiver locationStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 수신된 인텐트 액션이 위치 모드 변경인지 확인
            if (Objects.equals(intent.getAction(), "android.location.MODE_CHANGED")) {
                // EXTRA로 전달된 LOCATION_ENABLED 플래그를 꺼내서 상태 변경 처리
                boolean enabled = intent.getBooleanExtra("android.location.extra.LOCATION_ENABLED", false);
                LocationManagerHelper.this.onStateChanged(enabled);
            }
        }
    };

    // 외부에 노출할 콜백 인터페이스: 위치가 활성화되었는지 여부를 알리기 위함
    public interface Listener {
        /**
         * @param enabled
         *   true: GPS 또는 network provider 중 하나 이상 활성화됨
         *   false: 두 위치 제공자 모두 비활성화됨
         */
        void onLocationStateChanged(boolean enabled);
    }

    /**
     * 생성자
     * @param context  브로드캐스트 등록 및 시스템 서비스 접근을 위한 Context
     */
    public LocationManagerHelper(Context context) {
        this.mContext = context;
        // Context로부터 LocationManager(system service) 인스턴스를 얻어온다
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * 기기에서 LocationManager가 지원되는지 확인
     * @return LocationManager 객체가 null이 아니면 지원(true), 아니면 false
     */
    public boolean isSupported() {
        return this.locationManager != null;
    }

    /**
     * GPS 또는 네트워크 기반 위치 제공자 중 하나라도 활성화되어 있는지 확인
     * @return 둘 중 하나 이상 활성화되어 있으면 true, 모두 비활성화면 false
     */
    public boolean isEnabled() {
        // "gps" 프로바이더 활성화 여부 OR "network" 프로바이더 활성화 여부
        return this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * 외부에서 Listener 등록
     * @param listener 위치 상태 변경 시 콜백을 받기 위한 Listener
     */
    public void registerListener(Listener listener) {
        this.mListener = listener;
        // MODE_CHANGED 액션의 인텐트를 받을 수 있도록 BroadcastReceiver를 등록
        IntentFilter filter = new IntentFilter("android.location.MODE_CHANGED");
        this.mContext.registerReceiver(this.locationStateChangeReceiver, filter);
    }

    /**
     * 등록된 Listener 해제 및 BroadcastReceiver 언레지스터
     */
    public void unregisterListener() {
        // 콜백 참조 해제
        this.mListener = null;
        // 더 이상 위치 모드 변경을 수신하지 않도록 해제
        this.mContext.unregisterReceiver(this.locationStateChangeReceiver);
    }

    /**
     * BroadcastReceiver가 상태 변화를 감지하면 호출됨
     * @param enabled  위치 제공자 활성화 여부
     */
    public void onStateChanged(final boolean enabled) {
        // UI 스레드(메인 루퍼)에서 실제 콜백 호출이 일어나도록 핸들러로 포스트
        new Handler(Looper.getMainLooper()).post(() ->
                LocationManagerHelper.this.listenerOnLocationStateChanged(enabled)
        );
    }

    /**
     * 메인 스레드에서 실제 Listener 콜백을 호출
     * @param enabled  위치 제공자 활성화 여부
     */
    void listenerOnLocationStateChanged(boolean enabled) {
        Listener listener = this.mListener;
        // Listener가 null이 아닐 때만 콜백 실행
        if (listener != null) {
            listener.onLocationStateChanged(enabled);
        }
    }
}
