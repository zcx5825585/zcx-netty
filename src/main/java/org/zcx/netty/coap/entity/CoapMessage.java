package org.zcx.netty.coap.entity;

import cn.hutool.core.util.HexUtil;
import org.zcx.netty.coap.common.CoapContentFormat;
import org.zcx.netty.coap.common.CoapMessageType;
import org.zcx.netty.coap.common.CoapOptionType;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
    private List<InetSocketAddress> groupSenders;

    public CoapMessage createAck(int messageCode) {
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

        if (this.getOptions() != null) {
            //响应option
            if (!this.getOptions().containsKey(CoapOptionType.CONTENT_FORMAT)) {
                options.putObject(CoapOptionType.CONTENT_FORMAT, CoapContentFormat.APP_JSON);
            }
            if (this.getOptions().containsKey(CoapOptionType.OBSERVE)) {
                options.put(CoapOptionType.URI_PATH, this.getOptions().get(CoapOptionType.URI_PATH));
            }
            if (this.getOptions().containsKey(CoapOptionType.BLOCK_1)) {
                byte[] block = this.getOptions().get(CoapOptionType.BLOCK_1);
                options.put(CoapOptionType.BLOCK_1, block);
            }
        }
        return ack;
    }

    @Override
    public String toString() {

        return "CoapMessage{" +
                "version=" + version +
                ",\tmessageType=" + messageType +
                ",\ttokenLength=" + tokenLength +
                ",\tmessageCode=" + messageCode +
                ",\tmessageID=" + messageID +
                ",\ttoken='" + getTokenString() + '\'' +
                (options != null ? ",\toptions=" + options.toString() : "") +
                ", payload='" + payload + '\'' +
                '}';
    }

    public InetSocketAddress getSender() {
        return sender;
    }

    public void setSender(InetSocketAddress sender) {
        this.sender = sender;
    }

    public List<InetSocketAddress> getGroupSenders() {
        return groupSenders;
    }

    public void setGroupSenders(List<InetSocketAddress> groupSenders) {
        this.groupSenders = groupSenders;
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

    public String getTokenString() {
        return HexUtil.encodeHexStr(token);
    }

    public String getResourceUri() {
        String uri = "";
        if (this.getOptions().containsKey(11)) {
            uri = new String(this.getOptions().get(11), StandardCharsets.UTF_8);
        }
        return uri;
    }

    public String getCacheKey() {
        if (this.getOptions().containsKey(CoapOptionType.OBSERVE)) {
            String uri = getResourceUri();
            String senderString = getSenderString();
            return uri + "-" + senderString;
        } else {
            return HexUtil.encodeHexStr(token);
        }
//        String uri = new String(this.getOptions().get(11), StandardCharsets.UTF_8);
//        return uri+"-"+this.sender.getAddress().getHostAddress()+":"+this.sender.getPort();
    }

    public String getSenderString() {
        String senderString = "";
        if (this.sender != null) {
            senderString = this.sender.getAddress().getHostAddress() + ":" + this.sender.getPort();
        }
        return senderString;
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
