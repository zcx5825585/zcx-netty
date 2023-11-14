package org.zcx.netty.client.mqtt.dto;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttQoS;

public class MyMqttMessage {
    private ByteBuf payload;
    private String topic;

    private MqttQoS qos = MqttQoS.AT_MOST_ONCE;
    private boolean retain = false;
    private long waitTime = 30 * 1000L;

    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public MqttQoS getQos() {
        return qos;
    }

    public void setQos(MqttQoS qos) {
        this.qos = qos;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }
}
