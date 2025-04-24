package com.growspace.sdk.utils;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.Ascii;

/**
 * Utils 클래스는 바이트 배열과 기본 타입(short, int, byte) 간의 변환,
 * 배열 조작 및 헥스 문자열 처리 등 다양하게 활용되는 유틸리티 메서드를 제공합니다.
 */
public class Utils {

    // 헥스 문자열 변환 시 사용할 문자 배열 ('0'–'9', 'A'–'F')
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * 단일 byte 값을 길이 1짜리 바이트 배열로 변환
     * @param b 변환할 byte 값
     * @return {b} 형태의 바이트 배열
     */
    public static byte[] byteToByteArray(byte b) {
        // & 0xFF를 통해 음수(byte) 값을 0–255 범위의 양수로 변환
        return new byte[]{ (byte)(b & 0xFF) };
    }

    /**
     * 32비트 int 값을 4바이트 배열(빅엔디언)로 변환
     * @param i 변환할 int 값
     * @return {(i>>24)&0xFF, (i>>16)&0xFF, (i>>8)&0xFF, i&0xFF}
     */
    public static byte[] intToByteArray(int i) {
        return new byte[]{
                (byte)((i >> 24) & 0xFF),  // 최상위 바이트
                (byte)((i >> 16) & 0xFF),
                (byte)((i >> 8) & 0xFF),
                (byte)(i & 0xFF)           // 최하위 바이트
        };
    }

    /**
     * 16비트 short 값을 2바이트 배열(빅엔디언)로 변환
     * @param s 변환할 short 값
     * @return {(s>>8)&0xFF, s&0xFF}
     */
    public static byte[] shortToByteArray(short s) {
        return new byte[]{
                (byte)((s >> 8) & 0xFF),   // 상위 바이트
                (byte)(s & 0xFF)           // 하위 바이트
        };
    }

    /**
     * 바이트 배열을 헥스 문자열("00"–"FF" 반복)로 변환
     * @param bArr 변환할 바이트 배열
     * @return 2*length 길이의 헥스 문자열
     */
    public static String byteArrayToHexString(byte[] bArr) {
        int length = bArr.length;
        // 각 바이트당 두 글자씩, 총 길이*2 크기의 char 배열 준비
        char[] cArr = new char[length * 2];
        for (int i = 0; i < length; i++) {
            // bArr[i] & 0xFF로 0–255 정수화
            int v = bArr[i] & 0xFF;
            int idx = i * 2;
            // 상위 4비트 → 첫 헥스 문자
            cArr[idx]     = HEX_ARRAY[v >>> 4];
            // 하위 4비트 → 두 번째 헥스 문자
            cArr[idx + 1] = HEX_ARRAY[v & 0x0F];
        }
        // char 배열을 String으로 생성하여 반환
        return new String(cArr);
    }

    /**
     * 헥스 문자열("0A1B" 등)을 바이트 배열로 변환
     * @param str 짝수 길이의 헥스 문자열
     * @return length/2 크기의 바이트 배열
     */
    public static byte[] hexStringtoByteArray(String str) {
        int length = str.length();
        byte[] bArr = new byte[length / 2];
        // 두 글자씩 읽어 Character.digit로 0–15 정수화 후 합산
        for (int i = 0; i < length; i += 2) {
            int hi = Character.digit(str.charAt(i), 16);
            int lo = Character.digit(str.charAt(i+1), 16);
            bArr[i/2] = (byte)((hi << 4) + lo);
        }
        return bArr;
    }

    /**
     * 바이트 배열을 int로 변환 (배열 길이에 따라 처리)
     * @param bArr 길이 1~4의 바이트 배열
     * @return 해당 바이트 배열을 빅엔디언 정수로 해석한 값
     * @throws IndexOutOfBoundsException 길이가 1~4가 아니면 예외 발생
     */
    public static int byteArrayToInt(byte[] bArr) {
        int high; byte low;
        switch (bArr.length) {
            case 1:
                // 단일 바이트 → 0–255 정수
                return bArr[0] & 0xFF;
            case 2:
                // (b0<<8) + b1
                high = (bArr[0] & 0xFF) << 8;
                low  = bArr[1];
                break;
            case 3:
                // (b0<<16) + (b1<<8) + b2
                high = ((bArr[0] & 0xFF) << 16) + ((bArr[1] & 0xFF) << 8);
                low  = bArr[2];
                break;
            case 4:
                // (b0<<24) + (b1<<16) + (b2<<8) + b3
                // Ascii.CAN == 24
                high = (bArr[0] << Ascii.CAN) + ((bArr[1] & 0xFF) << 16) + ((bArr[2] & 0xFF) << 8);
                low  = bArr[3];
                break;
            default:
                throw new IndexOutOfBoundsException("Invalid byte array length: " + bArr.length);
        }
        // high + (low & 0xFF)
        return high + (low & 0xFF);
    }

    /**
     * 바이트 배열을 short로 변환 (길이 1 또는 2만 지원)
     * @param bArr 길이 1 또는 2의 바이트 배열
     * @return 0–65535 범위의 short 값
     * @throws IndexOutOfBoundsException 길이가 1 또는 2가 아니면 예외
     */
    public static short byteArrayToShort(byte[] bArr) {
        if (bArr.length == 1) {
            return (short)(bArr[0] & 0xFF);
        } else if (bArr.length == 2) {
            return (short)(((bArr[0] & 0xFF) << 8) + (bArr[1] & 0xFF));
        } else {
            throw new IndexOutOfBoundsException("Invalid short byte array length: " + bArr.length);
        }
    }

    /**
     * 길이 1짜리 바이트 배열을 단일 byte로 변환
     * @param bArr 길이 1의 바이트 배열
     * @return 내부 바이트 값
     * @throws IndexOutOfBoundsException 길이가 1이 아니면 예외
     */
    public static byte byteArrayToByte(byte[] bArr) {
        if (bArr.length == 1) {
            return (byte)(bArr[0] & 0xFF);
        }
        throw new IndexOutOfBoundsException("Invalid byte array length: " + bArr.length);
    }

    /**
     * 두 바이트 배열의 부분 범위를 비교
     * @param a     비교 대상 첫 배열
     * @param aOff  첫 배열에서 비교 시작 오프셋
     * @param b     비교 대상 두 번째 배열
     * @param bOff  두 번째 배열에서 비교 시작 오프셋
     * @param len   비교할 바이트 수
     * @return 모든 바이트가 일치하면 true, 하나라도 다르면 false
     */
    public static boolean compareByteArrays(byte[] a, int aOff, byte[] b, int bOff, int len) {
        int i = aOff, j = bOff;
        // j < bOff+len 까지 반복
        while (j < bOff + len) {
            if (a[i] != b[j]) {
                return false;  // 불일치 발견 시 즉시 false
            }
            i++; j++;
        }
        // 비교 범위가 정확히 len개였는지 확인 후 true 반환
        return (i - aOff == len) && (j - bOff == len);
    }

    /**
     * 배열이 모두 0으로만 채워져 있는지 확인
     * @param bArr 검사할 배열
     * @return 모든 요소가 0이면 true, 하나라도 0이 아니면 false
     */
    public static boolean arrayIsAllZeros(byte[] bArr) {
        for (byte b : bArr) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 바이트 배열에서 일정 길이만큼 잘라내어 새로운 배열로 복사
     * @param src       원본 배열
     * @param length    복사할 길이
     * @param offset    원본에서 복사 시작 오프셋
     * @return 복사된 길이만큼의 새 배열
     */
    public static byte[] extract(byte[] src, int length, int offset) {
        byte[] dst = new byte[length];
        System.arraycopy(src, offset, dst, 0, length);
        return dst;
    }

    /**
     * 앞쪽의 선행 0 바이트를 제거하여 배열 크기를 줄임
     * @param bArr 원본 배열
     * @return 0이 아닌 바이트부터 시작하는 새 배열, 모두 0이면 null
     */
    public static byte[] trimLeadingZeros(byte[] bArr) {
        int idx = 0;
        // 첫 0이 아닌 인덱스 찾기
        while (idx < bArr.length && bArr[idx] == 0) {
            idx++;
        }
        // 전부 0일 경우 null 반환
        if (idx == bArr.length) {
            return null;
        }
        int newLen = bArr.length - idx;
        byte[] dst = new byte[newLen];
        System.arraycopy(bArr, idx, dst, 0, newLen);
        return dst;
    }

    /**
     * 두 배열 연결 (배열 중 하나가 null이면 나머지 반환)
     * @param a 첫 배열
     * @param b 두 번째 배열
     * @return a||b 연결 결과
     */
    public static byte[] concat(byte[] a, byte[] b) {
        if (a == null) return b;
        if (b == null) return a;
        byte[] dst = new byte[a.length + b.length];
        System.arraycopy(a, 0, dst, 0, a.length);
        System.arraycopy(b, 0, dst, a.length, b.length);
        return dst;
    }

    /**
     * 세 배열 연결 (null 처리 포함)
     */
    public static byte[] concat(byte[] a, byte[] b, byte[] c) {
        if (a == null) return concat(b, c);
        if (b == null) return concat(a, c);
        if (c == null) return concat(a, b);
        byte[] dst = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, dst, 0, a.length);
        System.arraycopy(b, 0, dst, a.length, b.length);
        System.arraycopy(c, 0, dst, a.length + b.length, c.length);
        return dst;
    }

    /**
     * 네 배열 연결 (null 처리 포함)
     */
    public static byte[] concat(byte[] a, byte[] b, byte[] c, byte[] d) {
        if (a == null) return concat(b, c, d);
        if (b == null) return concat(a, c, d);
        if (c == null) return concat(a, b, d);
        if (d == null) return concat(a, b, c);
        byte[] dst = new byte[a.length + b.length + c.length + d.length];
        System.arraycopy(a, 0, dst, 0, a.length);
        System.arraycopy(b, 0, dst, a.length, b.length);
        System.arraycopy(c, 0, dst, a.length + b.length, c.length);
        System.arraycopy(d, 0, dst, a.length + b.length + c.length, d.length);
        return dst;
    }

    /**
     * 바이트 배열을 역순으로 뒤집기
     * @param bArr 원본 배열
     * @return 뒤집힌 새 배열
     */
    public static byte[] revert(byte[] bArr) {
        int len = bArr.length;
        byte[] dst = new byte[len];
        for (int i = 0; i < len; i++) {
            dst[i] = bArr[(len - 1) - i];
        }
        return dst;
    }
}
