package com.github.vzakharchenko.backend;

import com.github.vzakharchenko.backend.frontend.FrontendNioEventLoopGroup;
import com.github.vzakharchenko.backend.frontend.handlers.FrontendChannelHandler;
import com.github.vzakharchenko.backend.frontend.handlers.FrontendConnectionHandler;
import com.github.vzakharchenko.backend.workers.WorkerNioEventLoopGroup;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.comparator.Comparators;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Component
public class AcceptorImpl implements Acceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptorImpl.class);


    private final ServerBootstrap serverBootstrap;
    private final int port;

    @Autowired
    public AcceptorImpl(@Value("${server.sql.port}") int port,
                        ServerBootstrap serverBootstrap,
                        List<FrontendChannelHandler> handlers,
                        WorkerNioEventLoopGroup eventLoopGroup,
                        FrontendNioEventLoopGroup frontendNioEventLoopGroup) {
        serverBootstrap.group(frontendNioEventLoopGroup, eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress("127.0.0.1", port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        handlers.stream().sorted((o1, o2) -> Comparators.comparable().compare(o1.order(), o2.order())).forEach(new Consumer<FrontendChannelHandler>() {
                            @Override
                            public void accept(FrontendChannelHandler handler) {
                                ch.pipeline().addLast(handler);
                            }
                        });
                    }
                });
        this.serverBootstrap = serverBootstrap;
        this.port = port;
    }

    @Override
    public void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    ChannelFuture future = serverBootstrap.bind().sync();
                    LOGGER.info("server bind on {}:{}", "0.0.0.0", port);
                    future.channel().closeFuture().sync();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                Connection conn = null;
                try {
                    conn =
                            DriverManager.getConnection("jdbc:mysql://localhost:9000/?ss=22", "admin", "admin");
                    Thread.sleep(2000);
                    FrontendConnectionHandler.test = true;
                    // Do something with the Connection
                } catch (SQLException ex) {
                    // handle any errors
                    System.out.println("SQLException: " + ex.getMessage());
                    System.out.println("SQLState: " + ex.getSQLState());
                    System.out.println("VendorError: " + ex.getErrorCode());
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        });

    }

}
