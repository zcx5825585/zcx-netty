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
        int szxTemp = bytes[length - 1] & 0x07;     // 取低3位
        int szx = 1 << (szxTemp + 4);
        return new int[]{num, m, szx};
    }

    public static byte[] reverseHandlerBlock(int[] values) {
        if (values == null || values.length != 3) {
            throw new IllegalArgumentException("Input int array must be of length 3.");
        }

        int num = values[0];
        int m = values[1];
//        int szx = values[2];
        int szx = (int) (Math.log(values[2]) / Math.log(2)) - 4;

//        // 检查 szx 是否有效（应为 16, 32, 64, 128...）
//        if ((szx & (szx - 1)) != 0 || szx < 16 || szx > 1 << 28) {
//            throw new IllegalArgumentException("Invalid szx value: " + szx);
//        }

        int length = 1;

        byte[] bytes = new byte[length];

        if (length == 1) {
            bytes[0] = (byte) (((num & 0x0F) << 4) | (m << 3) | (szx & 0x07));
        } else if (length == 2) {
            bytes[0] = (byte) ((num >> 4) & 0xFF);
            bytes[1] = (byte) (((num & 0x0F) << 4) | (m << 3) | (szx - 1 & 0x07));
        } else if (length == 3) {
            bytes[0] = (byte) ((num >> 12) & 0xFF);
            bytes[1] = (byte) ((num >> 4) & 0xFF);
            bytes[2] = (byte) (((num & 0x0F) << 4) | (m << 3) | (szx - 1 & 0x07));
        }

        return bytes;
    }
    public static int bytesToInt(byte[] byteArray) {
        int length = byteArray.length;
        int result = 0;

        // 检查数组长度，确保在1到4个字节之间
        if (length < 1 || length > 4) {
            throw new IllegalArgumentException("byteArray length must be between 1 and 4");
        }

        // 遍历数组，将每个字节转换为无符号整数并适当移位
        for (int i = 0; i < length; i++) {
            int byteValue = (byteArray[i] & 0xFF); // 将字节转换为无符号整数
            result |= (byteValue << ((length - 1 - i) * 8)); // 左移相应的位数
        }

        return result;
    }

    public static byte[] intToTwoBytes(int value) {
        byte[] byteArray = new byte[2];

        // 将整数的高8位（即前8位）转换为字节，并存储在byteArray[0]中
        byteArray[0] = (byte) ((value >> 8) & 0xFF);

        // 将整数的低8位（即后8位）转换为字节，并存储在byteArray[1]中
        byteArray[1] = (byte) (value & 0xFF);

        return byteArray;
    }
    public static byte[] intToBytes(int value) {
        if (value == 0) {
            return new byte[]{0x00}; // Special case for zero
        }

        byte[] byteArray = new byte[4];
        int byteCount = 0;

        // Determine the number of bytes needed
        boolean hasNonZeroByte = false;
        for (int i = 3; i >= 0; i--) {
            byte b = (byte) ((value >> (i * 8)) & 0xFF);
            if (b != 0 || hasNonZeroByte) {
                byteArray[byteCount++] = b;
                hasNonZeroByte = true;
            }
        }

        // Resize the array to the actual number of bytes needed
        byte[] result = new byte[byteCount];
        System.arraycopy(byteArray, 0, result, 0, byteCount);
        return result;
    }
    public static byte[] longToBytes(long value) {
        // Note that this code needs to stay compatible with GWT, which has known
        // bugs when narrowing byte casts of long values occur.
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= 8;
        }
        return result;
    }


    public static int getNextMessageId(int messageId){
        return messageId+1;
    }
}
