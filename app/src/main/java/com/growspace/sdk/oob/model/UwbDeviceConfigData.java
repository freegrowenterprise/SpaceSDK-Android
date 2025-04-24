package com.growspace.sdk.oob.model;

import android.util.Log;

import com.growspace.sdk.utils.Utils;

import java.io.Serializable;

/**
 * UwbDeviceConfigData 클래스는 UWB 장치로부터 수신한 설정 데이터를
 * 직렬화 가능한 형태로 변환하여 보관 및 재전송할 수 있도록 합니다.
 * - 필드: 스펙 버전, 칩 ID/펌웨어 버전, 미들웨어 버전, 지원 프로파일 및 역할, 장치 MAC 주소
 * - toByteArray(): 필드를 순차적으로 바이트 배열로 직렬화
 * - fromByteArray(): 바이트 배열을 파싱하여 객체로 역직렬화
 */
public class UwbDeviceConfigData implements Serializable {
    // 로그 출력 시 사용되는 태그 상수
    private static final String TAG = "UwbDeviceConfigData";

    // 칩 펌웨어 버전 (2바이트 배열)
    public byte[] chipFwVersion;
    // 칩 식별자 ID (2바이트 배열)
    public byte[] chipId;
    // UWB 장치의 MAC 주소 (바이트 배열, 길이는 호출부에서 정의)
    public byte[] deviceMacAddress;
    // 미들웨어 버전 (3바이트 배열)
    public byte[] mwVersion;
    // UWB 스펙 메이저 버전 (short, 2바이트)
    public short specVerMajor;
    // UWB 스펙 마이너 버전 (short, 2바이트)
    public short specVerMinor;
    // 지원하는 디바이스 Ranging 역할 비트마스크 (byte)
    public byte supportedDeviceRangingRoles;
    // 지원하는 UWB 프로파일 ID 비트마스크 (int, 4바이트)
    public int supportedUwbProfileIds;

    /**
     * 기본 생성자
     * - 칩 ID, 칩 펌웨어 버전, 미들웨어 버전 배열을 초기 크기로 할당
     * - 나머지 필드는 호출부에서 setter 또는 fromByteArray()로 설정
     */
    public UwbDeviceConfigData() {
        this.chipId = new byte[2];           // 2바이트 칩 ID 배열 초기화
        this.chipFwVersion = new byte[2];    // 2바이트 펌웨어 버전 배열 초기화
        this.mwVersion = new byte[3];        // 3바이트 미들웨어 버전 배열 초기화
    }

    /**
     * 모든 필드를 직접 설정하는 생성자
     * @param s             스펙 메이저 버전
     * @param s2            스펙 마이너 버전
     * @param bArr          칩 ID 배열
     * @param bArr2         칩 펌웨어 버전 배열
     * @param bArr3         미들웨어 버전 배열
     * @param i             지원 UWB 프로파일 ID 비트마스크
     * @param b             지원 디바이스 Ranging 역할 비트마스크
     * @param bArr4         디바이스 MAC 주소 배열
     */
    public UwbDeviceConfigData(short s, short s2, byte[] bArr, byte[] bArr2, byte[] bArr3, int i, byte b, byte[] bArr4) {
        this.specVerMajor = s;
        this.specVerMinor = s2;
        this.chipId = bArr;
        this.chipFwVersion = bArr2;
        this.mwVersion = bArr3;
        this.supportedUwbProfileIds = i;
        this.supportedDeviceRangingRoles = b;
        this.deviceMacAddress = bArr4;
    }

    /** @return 스펙 메이저 버전(short) */
    public short getSpecVerMajor() {
        return this.specVerMajor;
    }

    /** @param s 설정할 스펙 메이저 버전(short) */
    public void setSpecVerMajor(short s) {
        this.specVerMajor = s;
    }

    /** @return 스펙 마이너 버전(short) */
    public short getSpecVerMinor() {
        return this.specVerMinor;
    }

    /** @param s 설정할 스펙 마이너 버전(short) */
    public void setSpecVerMinor(short s) {
        this.specVerMinor = s;
    }

    /** @return 칩 ID 바이트 배열(길이 2) */
    public byte[] getChipId() {
        return this.chipId;
    }

    /** @param bArr 설정할 칩 ID 배열(길이 2) */
    public void setChipId(byte[] bArr) {
        this.chipId = bArr;
    }

    /** @return 칩 펌웨어 버전 바이트 배열(길이 2) */
    public byte[] getChipFwVersion() {
        return this.chipFwVersion;
    }

    /** @param bArr 설정할 칩 펌웨어 버전 배열(길이 2) */
    public void setChipFwVersion(byte[] bArr) {
        this.chipFwVersion = bArr;
    }

    /** @return 미들웨어 버전 바이트 배열(길이 3) */
    public byte[] getMwVersion() {
        return this.mwVersion;
    }

    /** @param bArr 설정할 미들웨어 버전 배열(길이 3) */
    public void setMwVersion(byte[] bArr) {
        this.mwVersion = bArr;
    }

    /** @return 지원하는 UWB 프로파일 ID 비트마스크(int) */
    public int getSupportedUwbProfileIds() {
        return this.supportedUwbProfileIds;
    }

    /** @param i 설정할 지원 UWB 프로파일 ID 비트마스크(int) */
    public void setSupportedUwbProfileIds(int i) {
        this.supportedUwbProfileIds = i;
    }

    /** @return 지원하는 디바이스 Ranging 역할 비트마스크(byte) */
    public byte getSupportedDeviceRangingRoles() {
        return this.supportedDeviceRangingRoles;
    }

    /** @param b 설정할 지원 디바이스 Ranging 역할 비트마스크(byte) */
    public void setSupportedDeviceRangingRoles(byte b) {
        this.supportedDeviceRangingRoles = b;
    }

    /** @return 디바이스 MAC 주소 바이트 배열 */
    public byte[] getDeviceMacAddress() {
        return this.deviceMacAddress;
    }

    /** @param bArr 설정할 디바이스 MAC 주소 바이트 배열 */
    public void setDeviceMacAddress(byte[] bArr) {
        this.deviceMacAddress = bArr;
    }

    /**
     * 모든 필드를 순차적으로 병합하여 하나의 바이트 배열로 직렬화
     * 순서:
     *  specVerMajor(2B) → specVerMinor(2B) → chipId(2B) → chipFwVersion(2B)
     *  → mwVersion(3B) → supportedUwbProfileIds(4B) → supportedDeviceRangingRoles(1B)
     *  → deviceMacAddress(길이 고정)
     * @return 직렬화된 바이트 배열
     */
    public byte[] toByteArray() {
        return Utils.concat(
                Utils.concat(
                        Utils.concat(
                                Utils.concat(
                                        Utils.concat(
                                                Utils.concat(
                                                        Utils.concat(
                                                                Utils.concat(
                                                                        null,
                                                                        Utils.shortToByteArray(this.specVerMajor)
                                                                ),
                                                                Utils.shortToByteArray(this.specVerMinor)
                                                        ),
                                                        this.chipId
                                                ),
                                                this.chipFwVersion
                                        ),
                                        this.mwVersion
                                ),
                                Utils.intToByteArray(this.supportedUwbProfileIds)
                        ),
                        Utils.byteToByteArray(this.supportedDeviceRangingRoles)
                ),
                this.deviceMacAddress
        );
    }

    /**
     * 바이트 배열을 파싱하여 UwbDeviceConfigData 객체로 역직렬화
     * - extract(offset, length) 메서드를 이용해 각 필드별로 바이트 슬라이스
     * - Utils.byteArrayToShort/Int/Byte를 사용해 기본 타입으로 변환
     * - 예외 발생 시 null 반환
     * @param bArr 입력 바이트 배열
     * @return 파싱된 UwbDeviceConfigData 객체 또는 실패 시 null
     */
    public static UwbDeviceConfigData fromByteArray(byte[] bArr) {
        try {
            UwbDeviceConfigData uwbDeviceConfigData = new UwbDeviceConfigData();
            // 스펙 메이저 버전: 첫 2바이트(0~1)
            uwbDeviceConfigData.setSpecVerMajor(
                    Utils.byteArrayToShort(Utils.extract(bArr, 2, 0))
            );
            // 스펙 마이너 버전: 다음 2바이트(2~3)
            uwbDeviceConfigData.setSpecVerMinor(
                    Utils.byteArrayToShort(Utils.extract(bArr, 2, 2))
            );
            // 칩 ID: 2바이트(4~5)
            uwbDeviceConfigData.setChipId(
                    Utils.extract(bArr, 2, 4)
            );
            // 칩 펌웨어 버전: 2바이트(6~7)
            uwbDeviceConfigData.setChipFwVersion(
                    Utils.extract(bArr, 2, 6)
            );
            // 미들웨어 버전: 3바이트(8~10)
            uwbDeviceConfigData.setMwVersion(
                    Utils.extract(bArr, 3, 8)
            );
            // 지원 프로파일 ID: 4바이트(11~14)
            uwbDeviceConfigData.setSupportedUwbProfileIds(
                    Utils.byteArrayToInt(Utils.extract(bArr, 4, 11))
            );
            // 지원 디바이스 역할: 1바이트(15)
            uwbDeviceConfigData.setSupportedDeviceRangingRoles(
                    Utils.byteArrayToByte(Utils.extract(bArr, 1, 15))
            );
            // 디바이스 MAC 주소: 2바이트(16~17)
            uwbDeviceConfigData.setDeviceMacAddress(
                    Utils.extract(bArr, 2, 16)
            );
            return uwbDeviceConfigData;
        } catch (Exception unused) {
            // 파싱 도중 오류 발생 시 로그 출력하고 null 반환
            Log.e(TAG, "Invalid data received!");
            return null;
        }
    }
}
