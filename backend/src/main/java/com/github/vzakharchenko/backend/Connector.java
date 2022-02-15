package com.github.vzakharchenko.backend;

import com.github.vzakharchenko.backend.mysql.protocol.ErrorPacket;
import com.github.vzakharchenko.backend.utils.IpUtil;
import com.github.vzakharchenko.backend.workers.WorkerNioEventLoopGroup;
import com.github.vzakharchenko.backend.workers.handler.WorkerChannelHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.comparator.Comparators;

import java.net.InetSocketAddress;
import java.util.List;

@Component
public class Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connector.class);

    private final Bootstrap bootstrap;

    private Channel backend;

    @Autowired
    public Connector(Bootstrap bootstrap, List<WorkerChannelHandler> handlers, WorkerNioEventLoopGroup eventLoopGroup) {
        this.bootstrap = bootstrap;
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        handlers.stream().sorted((o1, o2) -> Comparators.comparable().compare(o1.order(), o2.order()))
                                .forEach(handler -> ch.pipeline().addLast(handler));
                    }

                });
    }

    public void connectProxy(Channel frontend) {
        SQLSession.startSession(frontend, backend);
    }
    public void connect(String host, int port, final Channel frontend) {
       if (backend == null || !backend.isActive() || !backend.isOpen()){
           ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
           ChannelFutureListener listener = future1 -> {
               if (future1.isSuccess()) {
                   backend = future1.channel();
                   LOGGER.info("on channel connect future operationComplete, bind channel, frontend : [{}], backend : [{}]",
                           IpUtil.getRemoteAddress(frontend), IpUtil.getAddress(backend));
                   SQLSession.startSession(frontend, backend);
               } else {
                   LOGGER.error("channel connect fail", future1.cause());
                   ErrorPacket.build(2003, future1.cause().getMessage()).write(frontend, true);
               }
           };
           future.addListener(listener);
       } else {
           SQLSession.startSession(frontend, backend);
       }
    }
}
