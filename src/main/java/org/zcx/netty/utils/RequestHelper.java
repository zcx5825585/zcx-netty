package org.zcx.netty.utils;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class RequestHelper {


    /**
     * http 响应结果
     *
     * @param ctx
     * @param data
     * @param <T>
     */
    public static <T extends Serializable> void doSend(ChannelHandlerContext ctx, T data) {
        doSend(ctx, HttpResponseStatus.OK, data);
    }

    /**
     * http 响应结果
     *
     * @param ctx
     * @param data
     * @param <T>
     */
    public static <T extends Serializable> void doSend(ChannelHandlerContext ctx, HttpResponseStatus status, T data) {
        FullHttpResponse response = createResponse(status, data, false, null);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    private static <T extends Serializable> FullHttpResponse createResponse(HttpResponseStatus status, T data, boolean buildError, Throwable e) {
        String json = JSON.toJSONString(data);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(json, StandardCharsets.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        return response;
    }


    /**
     * 发送
     */
    public static void sendTxt(ChannelHandlerContext ctx, String data) {
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(data, StandardCharsets.UTF_8)
        );
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
