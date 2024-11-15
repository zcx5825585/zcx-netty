package org.zcx.netty.coap.entity;

import com.alibaba.fastjson.JSON;
import org.zcx.netty.coap.common.CoapContentFormat;
import org.zcx.netty.coap.common.CoapMessageType;
import org.zcx.netty.coap.common.CoapOptionType;
import org.zcx.netty.coap.utils.BytesUtils;

import java.net.InetSocketAddress;

public class CoapMessage {
    private int version;
    private int messageType;
    private int tokenLength;
    private int messageCode;
    private int messageID;
    private byte[] token;
    private CoapMessageOptions options;
    private String payload;
    private InetSocketAddress sender;

    public CoapMessage createAck(){
        return createAck(69);
    }

    public CoapMessage createAck(int messageCode){
        CoapMessage ack = new CoapMessage();
        ack.setVersion(1);
        ack.setMessageType(CoapMessageType.ACK);
        ack.setTokenLength(this.tokenLength);
        ack.setMessageCode(messageCode);
        ack.setMessageID(this.getMessageID());
        ack.setToken(this.token);
        ack.setSender(this.sender);
        CoapMessageOptions options = new CoapMessageOptions();
        ack.setOptions(options);

        //响应option
        if (this.getOptions().containsKey(CoapOptionType.CONTENT_FORMAT)) {
            options.putEmpty(CoapOptionType.CONTENT_FORMAT);
        }else {
            options.putObject(CoapOptionType.CONTENT_FORMAT, CoapContentFormat.APP_JSON);
        }
        if (this.getOptions().containsKey(CoapOptionType.BLOCK_1)) {
            byte[] block = this.getOptions().get(CoapOptionType.BLOCK_1);
            options.put(CoapOptionType.BLOCK_1, block);
        }
        return ack;
    }

    @Override
    public String toString() {

        return "CoapMessage{" +
                "version=" + version +
                ", messageType=" + messageType +
                ", tokenLength=" + tokenLength +
                ", messageCode=" + messageCode +
                ", messageID=" + messageID +
                ", token='" + token + '\'' +
                ", options=" + JSON.toJSONString(options) +
                ", payload='" + payload + '\'' +
                '}';
    }

    public InetSocketAddress getSender() {
        return sender;
    }

    public void setSender(InetSocketAddress sender) {
        this.sender = sender;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getTokenLength() {
        return tokenLength;
    }

    public void setTokenLength(int tokenLength) {
        this.tokenLength = tokenLength;
    }

    public int getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public CoapMessageOptions getOptions() {
        return options;
    }

    public void setOptions(CoapMessageOptions options) {
        this.options = options;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
