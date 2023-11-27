package dynamicBean.mqttTopicHandler;

import org.springframework.stereotype.Component;
import org.zcx.netty.common.bean.ClassRegisterInfo;
import org.zcx.netty.depended.mqtt.MqttTopicHandler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component("myMqttTopicHandler")
public class MyMqttTopicHandler implements MqttTopicHandler {

    @Override
    public Object handleMassage(String fullTopic,String msg) {
        System.out.println("myMqttTopicHandler 处理消息"+msg);
        return null;
    }

}
