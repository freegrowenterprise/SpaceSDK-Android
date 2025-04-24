package com.growspace.sdk.oob;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.Ascii;
import com.growspace.sdk.utils.Utils;

/**
 * OoBHelper 클래스는 UWB 장치와 폰 간의 Out-of-Band(OoB) 메시지를
 * 생성(buildOoBMessage)하고, 수신된 메시지에서 페이로드를 추출(getValue)하는
 * 유틸리티 메서드를 제공합니다.
 */
public class OoBHelper {

    /**
     * MessageId 열거형은 UWB OoB 메시지의 타입 식별자(ID)를 정의합니다.
     * 각각의 값은 1바이트(messageId)로 전송되며, 고유한 역할을 가집니다.
     */
    public enum MessageId {
        /** UWB 장치 구성 데이터를 전송할 때 사용되는 메시지 ID (1) */
        uwbDeviceConfigurationData((byte) 1),
        /** UWB 거리 측정 세션이 시작되었음을 알리는 메시지 ID (2) */
        uwbDidStart((byte) 2),
        /** UWB 거리 측정 세션이 중지되었음을 알리는 메시지 ID (3) */
        uwbDidStop((byte) 3),
        /** 초기화 명령을 전송할 때 사용되는 메시지 ID (-91) */
        initialize((byte) -91),
        /** 폰 측 설정 데이터를 전송할 때 사용되는 메시지 ID (11, Ascii VT) */
        uwbPhoneConfigurationData(Ascii.VT),
        /** 세션 중지를 요청할 때 사용되는 메시지 ID (12, Ascii FF) */
        stop(Ascii.FF);

        // 실제 메시지 바이트 값
        private final byte messageId;

        /**
         * 생성자: enum 상수에 대응하는 바이트 ID 값을 설정
         * @param b 전송할 메시지 ID 바이트
         */
        MessageId(byte b) {
            this.messageId = b;
        }

        /**
         * @return 이 enum 상수에 매핑된 1바이트 메시지 ID
         */
        public byte getMessageId() {
            return this.messageId;
        }
    }

    /**
     * MessageIdLegacy 열거형은 구형 장치(Legacy) 초기화에 사용하는
     * 메시지 식별자(ID)를 정의합니다.
     */
    public enum MessageIdLegacy {
        /** Legacy 장치 초기화를 위한 메시지 ID (10) */
        initialize((byte) 10);

        private final byte messageId;

        /**
         * 생성자: legacy 메시지 ID를 설정
         * @param b legacy 메시지 ID 바이트
         */
        MessageIdLegacy(byte b) {
            this.messageId = b;
        }

        /**
         * @return legacy 메시지 ID 바이트
         */
        public byte getMessageId() {
            return this.messageId;
        }
    }

    /**
     * DevTypeLegacy 열거형은 레거시 OoB 지원 시 장치 종류를 구분하는 값입니다.
     * android(1), iphone(2) 두 가지 값을 정의합니다.
     */
    public enum DevTypeLegacy {
        /** Android 디바이스를 나타내는 값 (1) */
        android((byte) 1),
        /** iPhone 디바이스를 나타내는 값 (2) */
        iphone((byte) 2);

        // 디바이스 타입 식별용 바이트 값
        private final byte value;

        /**
         * 생성자: 디바이스 타입 식별자를 설정
         * @param b 디바이스 타입 바이트 값
         */
        DevTypeLegacy(byte b) {
            this.value = b;
        }

        /**
         * @return 이 디바이스 타입에 해당하는 바이트 값
         */
        public byte getValue() {
            return this.value;
        }
    }

    /**
     * ID만 포함한 OoB 메시지를 생성합니다.
     * 내부적으로 buildOoBMessage(id, null)을 호출합니다.
     * @param b 메시지 ID 바이트
     * @return 길이 1의 바이트 배열 {b}
     */
    public static byte[] buildOoBMessage(byte b) {
        return buildOoBMessage(b, null);
    }

    /**
     * ID와 페이로드(payload)를 결합한 OoB 메시지 바이트 배열을 생성합니다.
     * - 메시지 바이트(id) + payload 바이트 배열
     * - payload가 null이면 ID만으로 이루어진 배열을 반환
     *
     * @param b    메시지 ID 바이트
     * @param bArr 페이로드 바이트 배열 (null 허용)
     * @return {id, payload...} 형식의 바이트 배열
     */
    public static byte[] buildOoBMessage(byte b, byte[] bArr) {
        if (bArr != null) {
            // ID와 payload를 순차적으로 결합
            return Utils.concat(
                    Utils.byteToByteArray(b),
                    bArr
            );
        }
        // payload가 없으면 ID만 반환
        return Utils.byteToByteArray(b);
    }

    /**
     * 수신된 OoB 메시지에서 페이로드만 추출합니다.
     * - 메시지 배열의 첫 바이트가 기대하는 ID(b)와 일치하면,
     *   그 뒤의 모든 바이트를 반환
     * - 일치하지 않거나 배열이 null이면 null 반환
     *
     * @param bArr 수신된 전체 메시지 바이트 배열
     * @param b    기대하는 메시지 ID 바이트
     * @return 페이로드 바이트 배열 또는 null
     */
    public static byte[] getValue(byte[] bArr, byte b) {
        if (bArr != null && bArr.length > 0 && bArr[0] == b) {
            // 첫 바이트(ID) 이후 나머지를 추출 (offset=1, length=bArr.length-1)
            return Utils.extract(bArr, bArr.length - 1, 1);
        }
        // ID 불일치 또는 입력 오류 시 null 반환
        return null;
    }
}
