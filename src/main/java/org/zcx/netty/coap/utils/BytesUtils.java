package org.zcx.netty.coap.utils;

public class BytesUtils {

    public static int[] handlerBlock(byte[] bytes) {
        if (bytes == null || bytes.length < 1 || bytes.length > 3) {
            throw new IllegalArgumentException("Input byte array must be of length 1, 2, or 3.");
        }

        int num = 0;
        int length = bytes.length;

        if (length == 1) {
            num = (bytes[0] & 0xF0) >> 4; // 仅使用第一个（也是唯一一个）字节的前4位
        } else if (length == 2) {
            num = (bytes[0] & 0xFF) << 4 | ((bytes[1] & 0xF0) >> 4);
        } else if (length == 3) {
            num = (bytes[0] & 0xFF) << 12 | (bytes[1] & 0xFF) << 4 | ((bytes[2] & 0xF0) >> 4);
        }

        int m = (bytes[length - 1] & 0x08) >> 3; // 取倒数第4位
        int szx = bytes[length - 1] & 0x07;     // 取低3位

        return new int[]{num, m, szx};
    }

    public static int bytesToInt(byte[] byteArray) {
        int length = byteArray.length;

        // 将第一个字节（高位字节）转换为无符号，并左移8位
        int highByte = (byteArray[0] & 0xFF) << 8;

        if (length == 1) {
            return highByte;
        } else {
            int lowByte = byteArray[1] & 0xFF;
            return highByte | lowByte;
        }
    }
}
