package org.zcx.netty.mqtt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeepAliveTime implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(KeepAliveTime.class);

    private ChannelHandlerContext ctx;

    public KeepAliveTime(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        if (ctx != null && ctx.channel().isActive()) {
            MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessage mqttMessageBack = new MqttMessage(fixedHeader);
            ctx.writeAndFlush(mqttMessageBack);
        } else {
            log.error("心跳提醒：服务端连接异常~");
        }
    }
}
