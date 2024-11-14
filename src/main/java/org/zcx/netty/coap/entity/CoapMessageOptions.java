package org.zcx.netty.coap.entity;

import org.zcx.netty.coap.common.CoapContentFormat;
import org.zcx.netty.coap.common.CoapOptionType;

import java.util.HashMap;

public class CoapMessageOptions extends HashMap<Integer, byte[]> {

    //为指定key传入的空值
    public byte[] putEmpty(Integer key) {
        return super.put(key, new byte[0]);
    }

    //todo 根据key判断如何处理传入的value
    public byte[] putObject(Integer key, Object value) {
        switch (key){
            case CoapOptionType.CONTENT_FORMAT:
                byte[] byteArray = new byte[]{(byte) (CoapContentFormat.APP_JSON & 0xFF)};
                return super.put(key, byteArray);
            default:
                return null;
        }
    }

}
