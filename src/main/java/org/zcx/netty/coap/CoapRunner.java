package org.zcx.netty.coap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zcx.netty.coap.handlers.*;

@Component
public class CoapRunner {
    private static final Logger log = LoggerFactory.getLogger(CoapRunner.class);
    public static int port = 18020;

    //实现功能：
    //正常情况接收单块con/non请求并返回响应
    //接收block1分块请求并返回响应,分块、丢包错误处理
    //接收block1分块请求并返回响应，更改块大小的情况
    //接收请求后返回block2分块相应
    //接收请求后返回block2分块相应,更改块大小的情况
    //todo：
    //发送单块con/non请求并接收响应
    //发送block1分块请求并接收响应
    //发送请求后接收block2分块相应
    //观察模式
    // 客户端发送请求 options包含observe ,值为任意
    // 服务端响应    options包含observe 值为当前序号（版本？），在资源中维护 payload返回说明信息（无固定格式）
    // 服务端发送通知 options包含observe 值为当前序号（版本？），在资源中维护 payload为资源的值
    //请求：CoapMessage{version=1, messageType=0, tokenLength=8, messageCode=1, messageID=11388, token='bf765867e3c032bf', options=6:null;11:hello;, payload=''}
    //响应：CoapMessage{version=1, messageType=2, tokenLength=8, messageCode=69, messageID=11388, token='bf765867e3c032bf', options=6:4;12:null;, payload='/127.0.0.1:18020subscription successful,resource:/hello'}
    //响应：CoapMessage{version=1, messageType=1, tokenLength=8, messageCode=69, messageID=41076, token='bf765867e3c032bf', options=6:5;12:null;, payload='{"value":"4"}'}
    //
    // 观察模式和block2结合，此时服务器第一次返回的option 的token和观察模式的一样，但客户端请求后续消息的请求token为新token block2不使用token区分 而是uri+address+port  重复问题：再次请求会覆盖旧的
    //请求：CoapMessage{version=1,	messageType=0,	tokenLength=8,	messageCode=1,	messageID=45226,	token='bf3fda61cf25b510',	options=6:null;11:hello;, payload=''}
    //响应：CoapMessage{version=1,	messageType=2,	tokenLength=8,	messageCode=69,	messageID=45226,	token='bf3fda61cf25b510',	options=6:null;12:null;, payload='/127.0.0.1:18020subscription successful,resource:/hello'}
    //响应：CoapMessage{version=1,	messageType=1,	tokenLength=8,	messageCode=69,	messageID=42351,	token='bf3fda61cf25b510',	options=6:1;23:{NUM:0;M:1;SZX:512};12:null;28:2312;, payload='{"value":"{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hell'}
    //请求：CoapMessage{version=1,	messageType=0,	tokenLength=8,	messageCode=1,	messageID=45227,	token='58fdbeff186b35f1',	options=23:{NUM:1;M:0;SZX:512};11:hello;, payload=''}
    //响应：CoapMessage{version=1,	messageType=2,	tokenLength=8,	messageCode=69,	messageID=45227,	token='58fdbeff186b35f1',	options=23:{NUM:1;M:1;SZX:512};12:null;, payload='o\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\'}
    //请求：CoapMessage{version=1,	messageType=0,	tokenLength=8,	messageCode=1,	messageID=45228,	token='58fdbeff186b35f1',	options=23:{NUM:2;M:0;SZX:512};11:hello;, payload=''}
    //响应：CoapMessage{version=1,	messageType=2,	tokenLength=8,	messageCode=69,	messageID=45228,	token='58fdbeff186b35f1',	options=23:{NUM:2;M:1;SZX:512};12:null;, payload='"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respo'}
    //请求：CoapMessage{version=1,	messageType=0,	tokenLength=8,	messageCode=1,	messageID=45229,	token='58fdbeff186b35f1',	options=23:{NUM:3;M:0;SZX:512};11:hello;, payload=''}
    //响应：CoapMessage{version=1,	messageType=2,	tokenLength=8,	messageCode=69,	messageID=45229,	token='58fdbeff186b35f1',	options=23:{NUM:3;M:1;SZX:512};12:null;, payload='nd\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\'}
    //请求：CoapMessage{version=1,	messageType=0,	tokenLength=8,	messageCode=1,	messageID=45230,	token='58fdbeff186b35f1',	options=23:{NUM:4;M:0;SZX:512};11:hello;, payload=''}
    //响应：CoapMessage{version=1,	messageType=2,	tokenLength=8,	messageCode=69,	messageID=45230,	token='58fdbeff186b35f1',	options=23:{NUM:4;M:0;SZX:512};12:null;, payload='"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}{\"respond\":\"hello\"}"}'}
    //资源发现
    //丢包、返回错误等异常情况
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();  // 用来接收进来的连接
        Bootstrap server = new Bootstrap();//是一个启动NIO服务的辅助启动类
        server.group(bossGroup)
                .channel(NioDatagramChannel.class)  // 这里告诉Channel如何接收新的连接
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
//                        ch.pipeline().addLast(new BlockTestGatewayHandler());
//                        ch.pipeline().addLast(new TestGatewayHandler());

                        ch.pipeline().addLast(new CoapMessageEncoder());
                        ch.pipeline().addLast(new CoapMessageDecoder());
                        ch.pipeline().addLast(new CoapRstHandler());
                        ch.pipeline().addLast(new ServerBlock2Handler());
                        ch.pipeline().addLast(new ServerBlock1Handler());
                        ch.pipeline().addLast(new ServerObserveHandler());
                        ch.pipeline().addLast(new MyCoapHandler());

                    }
                });
        server.option(ChannelOption.SO_BROADCAST, true);
        ChannelFuture f = server.bind(port).sync();// 绑定端口，开始接收进来的连接
        log.info(port + "服务端启动成功...");
        // 监听服务器关闭监听
        f.channel().closeFuture().addListener((future) -> {
            bossGroup.shutdownGracefully(); //关闭EventLoopGroup，释放掉所有资源包括创建的线程
        });
    }

}
