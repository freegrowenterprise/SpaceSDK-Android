package com.growspace.sdk.oob.model;

import android.util.Log;

import com.growspace.sdk.utils.Utils;

import java.io.Serializable;

/**
 * UwbPhoneConfigData 클래스는 폰 측 UWB 세션 설정 데이터를 보관하고
 * 바이트 배열 ↔ 객체 간 변환 기능(toByteArray(), fromByteArray())을 제공합니다.
 * - 필드: UWB 스펙 버전, 세션 ID, 프리앰블 인덱스, 채널, 프로파일 ID, 디바이스 역할, 폰 MAC 주소
 * - toByteArray(): 모든 필드를 순서대로 직렬화하여 전송 가능 형태로 변환
 * - fromByteArray(): 받은 바이트 배열에서 필드별 데이터를 추출하여 객체로 복원
 */
public class UwbPhoneConfigData implements Serializable {
    // 로그 태그: 파싱 중 오류 등 로깅용
    private static final String TAG = "UwbPhoneConfigData";

    // UWB 스펙 메이저 버전 (2바이트)
    short specVerMajor;
    // UWB 스펙 마이너 버전 (2바이트)
    short specVerMinor;
    // 세션 식별자 (4바이트 정수)
    int sessionId;
    // 프리앰블 인덱스 (1바이트)
    byte preambleIndex;
    // UWB 채널 (1바이트)
    byte channel;
    // UWB 프로파일 ID (1바이트)
    byte profileId;
    // 폰이 수행할 디바이스 Ranging 역할 (1바이트 비트마스크)
    byte deviceRangingRole;
    // 폰 MAC 주소 (2바이트 배열)
    byte[] phoneMacAddress;

    /**
     * 기본 생성자
     * - 모든 필드가 디폴트(0 또는 null) 상태로 초기화
     */
    public UwbPhoneConfigData() {
    }

    /**
     * 모든 필드를 직접 지정하는 생성자
     * @param s      스펙 메이저 버전
     * @param s2     스펙 마이너 버전
     * @param i      세션 ID
     * @param b      프리앰블 인덱스
     * @param b2     채널
     * @param b3     프로파일 ID
     * @param b4     디바이스 Ranging 역할
     * @param bArr   폰 MAC 주소 바이트 배열
     */
    public UwbPhoneConfigData(short s, short s2, int i, byte b, byte b2, byte b3, byte b4, byte[] bArr) {
        this.specVerMajor      = s;
        this.specVerMinor      = s2;
        this.sessionId         = i;
        this.preambleIndex     = b;
        this.channel           = b2;
        this.profileId         = b3;
        this.deviceRangingRole = b4;
        this.phoneMacAddress   = bArr;
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

    /** @return 세션 ID(int) */
    public int getSessionId() {
        return this.sessionId;
    }

    /** @param i 설정할 세션 ID(int) */
    public void setSessionId(int i) {
        this.sessionId = i;
    }

    /** @return 프리앰블 인덱스(byte) */
    public byte getPreambleIndex() {
        return this.preambleIndex;
    }

    /** @param b 설정할 프리앰블 인덱스(byte) */
    public void setPreambleIndex(byte b) {
        this.preambleIndex = b;
    }

    /** @return UWB 채널(byte) */
    public byte getChannel() {
        return this.channel;
    }

    /** @param b 설정할 UWB 채널(byte) */
    public void setChannel(byte b) {
        this.channel = b;
    }

    /** @return 프로파일 ID(byte) */
    public byte getProfileId() {
        return this.profileId;
    }

    /** @param b 설정할 프로파일 ID(byte) */
    public void setProfileId(byte b) {
        this.profileId = b;
    }

    /** @return 디바이스 Ranging 역할(byte) */
    public byte getDeviceRangingRole() {
        return this.deviceRangingRole;
    }

    /** @param b 설정할 디바이스 Ranging 역할(byte) */
    public void setDeviceRangingRole(byte b) {
        this.deviceRangingRole = b;
    }

    /** @return 폰 MAC 주소(byte[]) */
    public byte[] getPhoneMacAddress() {
        return this.phoneMacAddress;
    }

    /** @param bArr 설정할 폰 MAC 주소 배열(byte[]) */
    public void setPhoneMacAddress(byte[] bArr) {
        this.phoneMacAddress = bArr;
    }

    /**
     * 객체를 바이트 배열로 직렬화
     * 순서:
     *  specVerMajor (2B) →
     *  specVerMinor (2B) →
     *  sessionId (4B) →
     *  preambleIndex (1B) →
     *  channel (1B) →
     *  profileId (1B) →
     *  deviceRangingRole (1B) →
     *  phoneMacAddress (2B)
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
                                                        Utils.intToByteArray(this.sessionId)
                                                ),
                                                Utils.byteToByteArray(this.preambleIndex)
                                        ),
                                        Utils.byteToByteArray(this.channel)
                                ),
                                Utils.byteToByteArray(this.profileId)
                        ),
                        Utils.byteToByteArray(this.deviceRangingRole)
                ),
                this.phoneMacAddress
        );
    }

    /**
     * 바이트 배열을 파싱하여 UwbPhoneConfigData 객체로 복원
     * - extract(buffer, length, offset)로 각 필드별 영역 추출
     * - Utils.byteArrayToShort/Int/Byte로 기본 타입 변환
     * - 예외 발생 시 null 반환
     * 필드별 바이트 위치:
     *  specVerMajor: offset=0, length=2
     *  specVerMinor: offset=2, length=2
     *  sessionId:    offset=4, length=4
     *  preambleIndex:offset=8, length=1
     *  channel:      offset=9, length=1
     *  profileId:    offset=10,length=1
     *  deviceRangingRole:offset=11,length=1
     *  phoneMacAddress:   offset=12,length=2
     * @param bArr 입력 바이트 배열
     * @return 복원된 UwbPhoneConfigData 또는 실패 시 null
     */
    public static UwbPhoneConfigData fromByteArray(byte[] bArr) {
        try {
            UwbPhoneConfigData data = new UwbPhoneConfigData();
            // 스펙 메이저 버전: 첫 2바이트
            data.setSpecVerMajor(Utils.byteArrayToShort(Utils.extract(bArr, 2, 0)));
            // 스펙 마이너 버전: 다음 2바이트
            data.setSpecVerMinor(Utils.byteArrayToShort(Utils.extract(bArr, 2, 2)));
            // 세션 ID: 4바이트
            data.setSessionId(Utils.byteArrayToShort(Utils.extract(bArr, 4, 4)));
            // 프리앰블 인덱스: 1바이트
            data.setPreambleIndex(Utils.byteArrayToByte(Utils.extract(bArr, 1, 8)));
            // 채널: 1바이트
            data.setChannel(Utils.byteArrayToByte(Utils.extract(bArr, 1, 9)));
            // 프로파일 ID: 1바이트
            data.setProfileId(Utils.byteArrayToByte(Utils.extract(bArr, 1, 10)));
            // 디바이스 Ranging 역할: 1바이트
            data.setDeviceRangingRole(Utils.byteArrayToByte(Utils.extract(bArr, 1, 11)));
            // 폰 MAC 주소: 2바이트
            data.setPhoneMacAddress(Utils.extract(bArr, 2, 12));
            return data;
        } catch (Exception unused) {
            // 잘못된 데이터 수신 시 로그 출력 후 null 반환
            Log.e(TAG, "Invalid data received!");
            return null;
        }
    }
}
