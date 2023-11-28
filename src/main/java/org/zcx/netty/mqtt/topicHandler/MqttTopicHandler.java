package org.zcx.netty.mqtt.topicHandler;

public interface MqttTopicHandler {

    public Object handleMassage(String fullTopic,String msg);
}
