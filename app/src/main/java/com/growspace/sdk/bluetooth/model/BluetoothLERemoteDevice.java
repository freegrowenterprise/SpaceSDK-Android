/**
 * BluetoothLERemoteDevice 클래스는 블루투스 LE 원격 장치의 정보를 관리하는 모델 클래스입니다.
 * 이 클래스는 GATT 클라이언트와 송수신 특성에 대한 참조를 유지합니다.
 * 
 * 주요 기능:
 * 1. GATT 클라이언트 참조 관리
 * 2. 송수신 특성 참조 관리
 * 3. 장치 연결 상태 관리
 * 
 * 사용 방법:
 * 1. BluetoothLERemoteDevice 인스턴스 생성
 * 2. GATT 클라이언트 설정 (setBluetoothGatt)
 * 3. 송수신 특성 설정 (setTxCharacteristic, setRxCharacteristic)
 * 4. 필요한 경우 참조 획득 (getter 메서드 사용)
 */
package com.growspace.sdk.bluetooth.model;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class BluetoothLERemoteDevice {
    /**
     * GATT 클라이언트 - 블루투스 LE 장치와의 통신을 위한 핵심 객체
     * <p>
     * 주요 기능:
     * - 장치 연결 관리
     * - 서비스 및 특성 검색
     * - 데이터 송수신
     */
    private BluetoothGatt bluetoothGatt;
    
    /**
     * 수신(RX) 특성 - 데이터 수신을 위한 특성
     * <p>
     * 주요 기능:
     * - 장치로부터 데이터 수신
     * - 알림(Notification) 설정
     * - 데이터 읽기
     */
    private BluetoothGattCharacteristic rxCharacteristic;
    
    /**
     * 송신(TX) 특성 - 데이터 송신을 위한 특성
     * <p>
     * 주요 기능:
     * - 장치로 데이터 송신
     * - 쓰기 요청 처리
     * - 데이터 쓰기
     */
    private BluetoothGattCharacteristic txCharacteristic;

    /**
     * 기본 생성자
     * <p>
     * 초기화 과정:
     * 1. 모든 필드를 null로 초기화
     */
    public BluetoothLERemoteDevice() {
    }

    /**
     * 매개변수가 있는 생성자
     * 
     * @param bluetoothGatt GATT 클라이언트 - 장치와의 통신을 위한 객체
     * @param bluetoothGattCharacteristic 송신(TX) 특성 - 데이터 송신을 위한 특성
     * @param bluetoothGattCharacteristic2 수신(RX) 특성 - 데이터 수신을 위한 특성
     * <p>
     * 초기화 과정:
     * 1. GATT 클라이언트 설정
     * 2. 송신 특성 설정
     * 3. 수신 특성 설정
     */
    public BluetoothLERemoteDevice(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, BluetoothGattCharacteristic bluetoothGattCharacteristic2) {
        this.bluetoothGatt = bluetoothGatt;
        this.txCharacteristic = bluetoothGattCharacteristic;
        this.rxCharacteristic = bluetoothGattCharacteristic2;
    }

    /**
     * GATT 클라이언트를 반환합니다.
     * 
     * @return GATT 클라이언트 - 장치와의 통신을 위한 객체
     * <p>
     * 사용 시점:
     * - 장치와의 통신이 필요한 경우
     * - 서비스 검색이 필요한 경우
     * - 연결 상태 확인이 필요한 경우
     */
    public BluetoothGatt getBluetoothGatt() {
        return this.bluetoothGatt;
    }

    /**
     * GATT 클라이언트를 설정합니다.
     * 
     * @param bluetoothGatt 설정할 GATT 클라이언트 - 장치와의 통신을 위한 객체
     * <p>
     * 사용 시점:
     * - 장치와 연결이 성공한 후
     * - GATT 클라이언트를 새로 생성한 후
     */
    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    /**
     * 송신(TX) 특성을 반환합니다.
     * 
     * @return 송신(TX) 특성 - 데이터 송신을 위한 특성
     * <p>
     * 사용 시점:
     * - 데이터를 장치로 송신할 때
     * - 특성의 속성을 확인할 때
     */
    public BluetoothGattCharacteristic getTxCharacteristic() {
        return this.txCharacteristic;
    }

    /**
     * 송신(TX) 특성을 설정합니다.
     * 
     * @param bluetoothGattCharacteristic 설정할 송신(TX) 특성 - 데이터 송신을 위한 특성
     * <p>
     * 사용 시점:
     * - 서비스 검색 후 특성을 찾았을 때
     * - 특성을 새로 설정해야 할 때
     */
    public void setTxCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.txCharacteristic = bluetoothGattCharacteristic;
    }

    /**
     * 수신(RX) 특성을 반환합니다.
     * 
     * @return 수신(RX) 특성 - 데이터 수신을 위한 특성
     * <p>
     * 사용 시점:
     * - 데이터를 장치로부터 수신할 때
     * - 특성의 속성을 확인할 때
     */
    public BluetoothGattCharacteristic getRxCharacteristic() {
        return this.rxCharacteristic;
    }

    /**
     * 수신(RX) 특성을 설정합니다.
     * 
     * @param bluetoothGattCharacteristic 설정할 수신(RX) 특성 - 데이터 수신을 위한 특성
     * <p>
     * 사용 시점:
     * - 서비스 검색 후 특성을 찾았을 때
     * - 특성을 새로 설정해야 할 때
     */
    public void setRxCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.rxCharacteristic = bluetoothGattCharacteristic;
    }
}
