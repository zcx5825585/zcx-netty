package tcpClient;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * I/O数据读写处理类
 * 蚂蚁舞
 */
@ChannelHandler.Sharable
public class TcpClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 从服务端收到新的数据时，这个方法会在收到消息时被调用
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception, IOException {
        if (msg == null) {
            return;
        }
        System.out.println("channelRead:read msg:" + msg.toString());
        //回应服务端
        //ctx.write("I got server message thanks server!");
    }

    /**
     * 从服务端收到新的数据、读取完成时调用
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws IOException {
        System.out.println("channelReadComplete");
        ctx.flush();
    }

    /**
     * 当出现 Throwable 对象才会被调用，即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException {
        System.out.println("exceptionCaught");
        cause.printStackTrace();
        ctx.close();//抛出异常，断开与客户端的连接
    }

    /**
     * 客户端与服务端第一次建立连接时 执行
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception, IOException {
        super.channelActive(ctx);
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = inSocket.getAddress().getHostAddress();
        System.out.println(clientIp);
//        new Thread(() -> {
//            int count =0;
//            while (true) {
//                count++;
//                try {
//                    Thread.sleep(3 * 1000L);
//                    ctx.writeAndFlush("test "+count);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    /**
     * 客户端与服务端 断连时 执行
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception, IOException {
        super.channelInactive(ctx);
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = inSocket.getAddress().getHostAddress();
        ctx.close(); //断开连接时，必须关闭，否则造成资源浪费
        System.out.println("channelInactive:" + clientIp);
    }


}