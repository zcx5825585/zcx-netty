package tcpClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpClient {
    private static final Logger log = LoggerFactory.getLogger(TcpClient.class);

    public static void main(String[] args) {
        String ip = "127.0.0.1";
        int port = 18022;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup()).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                        ch.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                        /**
                         * 自定义ChannelInboundHandlerAdapter
                         */
                        ch.pipeline().addLast(new TcpClientHandler());
                    }
                });
        bootstrap.connect(ip, port).addListener((future) -> {
            if (future.isSuccess()) {
                log.info("connect {}:{} success.", ip, port);
            } else {
                log.error("connect {}:{} fail", ip, port, future.cause());
            }
        });
    }
}
