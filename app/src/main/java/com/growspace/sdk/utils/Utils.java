package com.growspace.sdk.utils;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.Ascii;

public class Utils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static byte[] byteToByteArray(byte b) {
        return new byte[]{(byte) (b & 255)};
    }

    public static byte[] intToByteArray(int i) {
        return new byte[]{(byte) ((i >> 24) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 8) & 255), (byte) (i & 255)};
    }

    public static byte[] shortToByteArray(short s) {
        return new byte[]{(byte) ((s >> 8) & 255), (byte) (s & 255)};
    }

    public static String byteArrayToHexString(byte[] bArr) {
        int length = bArr.length;
        char[] cArr = new char[length * 2];
        for (int i = 0; i < length; i++) {
            int i2 = bArr[i] & 255;
            int i3 = i * 2;
            char[] cArr2 = HEX_ARRAY;
            cArr[i3] = cArr2[i2 >>> 4];
            cArr[i3 + 1] = cArr2[i2 & 15];
        }
        return new String(cArr);
    }

    public static byte[] hexStringtoByteArray(String str) {
        int length = str.length();
        byte[] bArr = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bArr;
    }

    public static int byteArrayToInt(byte[] bArr) {
        int i;
        byte b;
        if (bArr.length == 1) {
            return bArr[0] & 255;
        }
        if (bArr.length == 2) {
            i = (bArr[0] & 255) << 8;
            b = bArr[1];
        } else if (bArr.length == 3) {
            i = ((bArr[0] & 255) << 16) + ((bArr[1] & 255) << 8);
            b = bArr[2];
        } else if (bArr.length == 4) {
            i = (bArr[0] << Ascii.CAN) + ((bArr[1] & 255) << 16) + ((bArr[2] & 255) << 8);
            b = bArr[3];
        } else {
            throw new IndexOutOfBoundsException();
        }
        return i + (b & 255);
    }

    public static short byteArrayToShort(byte[] bArr) {
        if (bArr.length == 1) {
            return (short) (bArr[0] & 255);
        }
        if (bArr.length == 2) {
            return (short) (((bArr[0] & 255) << 8) + (bArr[1] & 255));
        }
        throw new IndexOutOfBoundsException();
    }

    public static byte byteArrayToByte(byte[] bArr) {
        if (bArr.length == 1) {
            return (byte) (bArr[0] & 255);
        }
        throw new IndexOutOfBoundsException();
    }

    public static boolean compareByteArrays(byte[] bArr, int i, byte[] bArr2, int i2, int i3) {
        int i4 = i;
        int i5 = i2;
        while (i5 < i3) {
            if (bArr[i4] != bArr2[i5]) {
                return false;
            }
            i4++;
            i5++;
        }
        return i4 - i == i3 && i5 - i2 == i3;
    }

    public static boolean arrayIsAllZeros(byte[] bArr) {
        for (byte b : bArr) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    public static byte[] extract(byte[] bArr, int i, int i2) {
        byte[] bArr2 = new byte[i];
        System.arraycopy(bArr, i2, bArr2, 0, i);
        return bArr2;
    }

    public static byte[] trimLeadingZeros(byte[] bArr) {
        int i = 0;
        while (i < bArr.length && bArr[i] == 0) {
            i++;
        }
        if (i == bArr.length) {
            return null;
        }
        int length = bArr.length - i;
        byte[] bArr2 = new byte[length];
        System.arraycopy(bArr, i, bArr2, 0, length);
        return bArr2;
    }

    public static byte[] concat(byte[] bArr, byte[] bArr2) {
        if (bArr == null) {
            return bArr2;
        }
        if (bArr2 == null) {
            return bArr;
        }
        byte[] bArr3 = new byte[bArr.length + bArr2.length];
        System.arraycopy(bArr, 0, bArr3, 0, bArr.length);
        System.arraycopy(bArr2, 0, bArr3, bArr.length, bArr2.length);
        return bArr3;
    }

    public static byte[] concat(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        if (bArr == null) {
            return concat(bArr2, bArr3);
        }
        if (bArr2 == null) {
            return concat(bArr, bArr3);
        }
        if (bArr3 == null) {
            return concat(bArr, bArr2);
        }
        byte[] bArr4 = new byte[bArr.length + bArr2.length + bArr3.length];
        System.arraycopy(bArr, 0, bArr4, 0, bArr.length);
        System.arraycopy(bArr2, 0, bArr4, bArr.length, bArr2.length);
        System.arraycopy(bArr3, 0, bArr4, bArr.length + bArr2.length, bArr3.length);
        return bArr4;
    }

    public static byte[] concat(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4) {
        if (bArr == null) {
            return concat(bArr2, bArr3, bArr4);
        }
        if (bArr2 == null) {
            return concat(bArr, bArr3, bArr4);
        }
        if (bArr3 == null) {
            return concat(bArr, bArr2, bArr4);
        }
        if (bArr4 == null) {
            return concat(bArr, bArr2, bArr3);
        }
        byte[] bArr5 = new byte[bArr.length + bArr2.length + bArr3.length + bArr4.length];
        System.arraycopy(bArr, 0, bArr5, 0, bArr.length);
        System.arraycopy(bArr2, 0, bArr5, bArr.length, bArr2.length);
        System.arraycopy(bArr3, 0, bArr5, bArr.length + bArr2.length, bArr3.length);
        System.arraycopy(bArr4, 0, bArr5, bArr.length + bArr2.length + bArr3.length, bArr4.length);
        return bArr5;
    }

    public static byte[] revert(byte[] bArr) {
        int length = bArr.length;
        byte[] bArr2 = new byte[length];
        for (int i = 0; i < length; i++) {
            bArr2[i] = bArr[(length - 1) - i];
        }
        return bArr2;
    }
}
