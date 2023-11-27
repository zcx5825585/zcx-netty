package org.zcx.netty.web.service;

import org.springframework.stereotype.Service;
import org.zcx.netty.handler.DynamicHandler;
import org.zcx.netty.handler.HandlerManager;
import org.zcx.netty.bean.ClassRegisterInfo;
import org.zcx.netty.common.exception.HandlerException;
import org.zcx.netty.handler.bootstrap.NettyTcpClientRunner;
import org.zcx.netty.handler.bootstrap.NettyTcpServerRunner;
import org.zcx.netty.web.dao.HandlerInfoDao;
import org.zcx.netty.web.entity.HandlerInfo;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.BindException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HandlerService {
    @Resource
    private NettyTcpServerRunner serverRunner;
    @Resource
    private NettyTcpClientRunner clientRunner;
    @Resource
    private HandlerManager handlerManager;
    @Resource
    private HandlerInfoDao handlerDao;

    @PostConstruct
    public void init() {
//        add(new HandlerInfo(1L, "httpHandler", 1L));
//        add(new HandlerInfo(2L, "tcpHandler", 1L));
//        add(new HandlerInfo(3L, "wsHandler", 1L));
//        add(new HandlerInfo(4L, "ws2Handler", 1L));
        add(new HandlerInfo(5L, "http2Handler", 1L));
//        add(new HandlerInfo(6L, "tcpClientHandler", 2L));
//        add(new HandlerInfo(7L, "singletonMqttClientHandler", 2L));
        //参数方式创建
        HandlerInfo configMqttClientHandler = new HandlerInfo(8L, "mqttClient_47_smartsite", 2L);
        configMqttClientHandler.setBaseHandlerName("multiTopicMqttClientHandler");
        configMqttClientHandler.setLoaderType("configurableBean");
        Map<String, Object> params = new HashMap<>();
        params.put("host", "47.105.217.47");
        params.put("port", 1883);
//        params.put("defaultTopic", "zcx/#");
        params.put("userName", "smartsite");
        params.put("password", "smartsite12347988");
        configMqttClientHandler.setArgs(params);
        add(configMqttClientHandler);

        //java字符串创建
        HandlerInfo http3Handler = new HandlerInfo(9L, "http3Handler", 1L);
        http3Handler.setLoaderType("scriptMemoryLoader");
        http3Handler.setJavaSrc("package dynamicBean.tcpServerHandler;\n" +
                "\n" +
                "import io.netty.buffer.ByteBuf;\n" +
                "import io.netty.buffer.ByteBufUtil;\n" +
                "import io.netty.channel.ChannelHandler;\n" +
                "import io.netty.channel.ChannelHandlerContext;\n" +
                "import io.netty.handler.codec.http.FullHttpRequest;\n" +
                "import io.netty.handler.codec.http.HttpObjectAggregator;\n" +
                "import io.netty.handler.codec.http.HttpServerCodec;\n" +
                "import org.apache.commons.logging.Log;\n" +
                "import org.apache.commons.logging.LogFactory;\n" +
                "import org.zcx.netty.handler.AbstractDynamicHandler;\n" +
                "import org.zcx.netty.handler.DynamicHandler;\n" +
                "import org.zcx.netty.handler.HandlerManager;\n" +
                "import org.zcx.netty.bean.TestBean;\n" +
                "import org.zcx.netty.common.utils.RequestHelper;\n" +
                "\n" +
                "import javax.annotation.Resource;\n" +
                "import java.nio.charset.StandardCharsets;\n" +
                "\n" +
                "@ChannelHandler.Sharable\n" +
                "public class Http3Handler extends AbstractDynamicHandler<FullHttpRequest> implements DynamicHandler {\n" +
                "\n" +
                "    @Resource\n" +
                "    private TestBean testBean;\n" +
                "\n" +
                "    private final Log log = LogFactory.getLog(this.getClass());\n" +
                "\n" +
                "    @Override\n" +
                "    public ChannelHandler[] initHandlers() {\n" +
                "        return new ChannelHandler[]{\n" +
                "                new HttpServerCodec(),\n" +
                "                new HttpObjectAggregator(512 * 1024),\n" +
                "                HandlerManager.getDynamicHandler(getHandlerName())\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    private int count = 0;\n" +
                "\n" +
                "    @Override\n" +
                "    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {\n" +
                "        count++;\n" +
                "        testBean.test();\n" +
                "        String uri = request.uri();\n" +
                "        String method = request.method().name();\n" +
                "        String body = getBody(request);\n" +
                "        log.info(\"接收到http消息 \\nhandler：\" + getHandlerName() + \"\\n\" + request.toString());\n" +
                "        RequestHelper.sendTxt(ctx, \"http3 connect \" + count);\n" +
                "    }\n" +
                "\n" +
                "    public static String getBody(FullHttpRequest request) {\n" +
                "        ByteBuf buf = request.content();\n" +
                "        byte[] bytes = ByteBufUtil.getBytes(buf);\n" +
                "        return new String(bytes, StandardCharsets.UTF_8);\n" +
                "    }\n" +
                "\n" +
                "}\n");
        add(http3Handler);


        handlerDao.setCurrentId(10L);

        //初始化handler
        List<HandlerInfo> handlerInfos = handlerDao.list(null);
        handlerInfos.stream().filter(HandlerInfo::getAutoRegister).forEach(one -> {
            register(one.getId());
        });

    }

    public HandlerInfo getById(Long id) {
        HandlerInfo handlerInfo = handlerDao.getById(id);
        handlerInfo.setHandler(HandlerManager.getDynamicHandler(handlerInfo.getHandlerName()));
        return handlerInfo;
    }

    public List<HandlerInfo> getHandlersByGroup(Long groupId) {
        HandlerInfo query = new HandlerInfo();
        query.setGroupId(groupId);
        List<HandlerInfo> handlerInfos = handlerDao.list(query);
        return handlerInfos.stream().peek(one -> {
            try {
                one.setHandler(HandlerManager.getDynamicHandler(one.getHandlerName()));
            } catch (Exception e) {
                one.setHandler(null);
            }
        }).collect(Collectors.toList());
    }

    public void add(HandlerInfo handlerInfo) {
        handlerDao.add(handlerInfo);
    }

    public DynamicHandler register(Long id) {
        HandlerInfo handlerInfo = handlerDao.getById(id);
        ClassRegisterInfo classRegisterInfo = new ClassRegisterInfo();
        classRegisterInfo.setBeanName(handlerInfo.getHandlerName());
        classRegisterInfo.setLoaderType(handlerInfo.getLoaderType());
        classRegisterInfo.setPackageName(handlerInfo.getPackageName());
        classRegisterInfo.setJavaSrc(handlerInfo.getJavaSrc());
        classRegisterInfo.setBaseBeanName(handlerInfo.getBaseHandlerName());
        classRegisterInfo.setArgs(handlerInfo.getArgs());
        classRegisterInfo.setReCompiler(false);
        classRegisterInfo.setSpringBean(true);
        return handlerManager.registerHandler(classRegisterInfo);
    }

    public void connect(Long handlerId) {
        HandlerInfo handlerInfo = getById(handlerId);
        DynamicHandler handler = handlerInfo.getHandler();
        if (handler == null) {
            throw new HandlerException("handler未初始化");
        }
        clientRunner.runHandlerAsClient(handler);
    }

    public void serverStart(Long handlerId,Integer port) throws Exception {
        HandlerInfo handlerInfo = getById(handlerId);
        DynamicHandler handler = handlerInfo.getHandler();
        if (handler == null) {
            throw new HandlerException("handler未初始化");
        }
        try {
            serverRunner.runHandlerAsServer(port, handler);
        }catch (BindException e){
            throw new HandlerException("端口已占用");
        }
    }
}
