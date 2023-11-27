package org.zcx.netty.depended.mqtt;

public interface MqttTopicHandler {

    public Object handleMassage(String fullTopic,String msg);
}
