package org.zcx.netty.coap.entity;

import org.zcx.netty.coap.common.CoapContentFormat;
import org.zcx.netty.coap.common.CoapOptionType;
import org.zcx.netty.coap.utils.BytesUtils;

import java.util.HashMap;

public class CoapMessageOptions extends HashMap<Integer, byte[]> {

    //为指定key传入的空值
    public byte[] putEmpty(Integer key) {
        return put(key, new byte[0]);
    }

    @Override
    public byte[] put(Integer key, byte[] value) {
        if (key == null || key < 0 || key > (0xFFFF + 269)) {
            throw new RuntimeException("optionNumber out of range");
        }
        if (value == null || value.length > (0xFFFF + 269)) {
            throw new RuntimeException("optionLength out of range");
        }
        return super.put(key, value);
    }

    //根据key判断如何处理传入的value
    public byte[] putObject(Integer key, Object value) {
        switch (key) {
            case CoapOptionType.CONTENT_FORMAT:
                return put(key, new byte[]{(byte) (CoapContentFormat.APP_JSON & 0xFF)});
            case CoapOptionType.BLOCK_1:
            case CoapOptionType.BLOCK_2:
                CoapBlock block = (CoapBlock) value;
                return put(key, BytesUtils.reverseHandlerBlock(block.getBlockInfo()));
            case CoapOptionType.SIZE_1:
            case CoapOptionType.SIZE_2:
                return put(key, BytesUtils.intToTwoBytes((Integer) value));
            case CoapOptionType.OBSERVE:
                return put(key, BytesUtils.intToBytes((Integer) value));
            default:
                return put(key, (byte[]) value);
        }
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Integer key : this.keySet()) {
            if (this.get(key).length < 1) {
                stringBuffer.append(key + ":null;");
                continue;
            }
            switch (key) {
                case CoapOptionType.CONTENT_FORMAT:
                    stringBuffer.append(key + ":" + new String(this.get(key)) + ";");
                    break;
                case CoapOptionType.BLOCK_1:
                case CoapOptionType.BLOCK_2:
                    int[] result = BytesUtils.handlerBlock(this.get(key));
                    stringBuffer.append(key + ":" + "{NUM:" + result[0] + ";M:" + result[1] + ";SZX:" + result[2] + "}" + ";");
                    break;
                case CoapOptionType.OBSERVE:
                    stringBuffer.append(key + ":" + BytesUtils.bytesToInt(this.get(key)) + "" + ";");
                    break;
                case CoapOptionType.SIZE_1:
                case CoapOptionType.SIZE_2:
                    stringBuffer.append(key + ":" + BytesUtils.bytesToInt(this.get(key)) + ";");
                    break;
                default:
                    stringBuffer.append(key + ":" + new String(this.get(key)) + ";");
                    break;
            }

        }
        return stringBuffer.toString();
    }
}
