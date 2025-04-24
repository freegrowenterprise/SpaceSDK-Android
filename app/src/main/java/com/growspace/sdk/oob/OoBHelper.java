package com.growspace.sdk.oob;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.Ascii;
import com.growspace.sdk.utils.Utils;

public class OoBHelper {

    public enum MessageId {
        uwbDeviceConfigurationData((byte) 1), uwbDidStart((byte) 2), uwbDidStop((byte) 3), initialize((byte) -91), uwbPhoneConfigurationData(Ascii.VT), stop(Ascii.FF);

        private final byte messageId;

        MessageId(byte b) {
            this.messageId = b;
        }

        public byte getMessageId() {
            return this.messageId;
        }
    }

    public enum MessageIdLegacy {
        initialize((byte) 10);

        private final byte messageId;

        MessageIdLegacy(byte b) {
            this.messageId = b;
        }

        public byte getMessageId() {
            return this.messageId;
        }
    }

    public enum DevTypeLegacy {
        android((byte) 1), iphone((byte) 2);

        private final byte value;

        DevTypeLegacy(byte b) {
            this.value = b;
        }

        public byte getValue() {
            return this.value;
        }
    }

    public static byte[] buildOoBMessage(byte b) {
        return buildOoBMessage(b, null);
    }

    public static byte[] buildOoBMessage(byte b, byte[] bArr) {
        if (bArr != null) {
            return Utils.concat(Utils.byteToByteArray(b), bArr);
        }
        return Utils.byteToByteArray(b);
    }

    public static byte[] getValue(byte[] bArr, byte b) {
        if (bArr != null && bArr[0] == b) {
            return Utils.extract(bArr, bArr.length - 1, 1);
        }
        return null;
    }
}
