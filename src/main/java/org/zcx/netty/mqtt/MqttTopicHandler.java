package org.zcx.netty.mqtt;

public interface MqttTopicHandler {

    public Object handleMassage(String fullTopic,String msg);
}
