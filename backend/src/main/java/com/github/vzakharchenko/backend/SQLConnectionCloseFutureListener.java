package com.github.vzakharchenko.backend;

import com.github.vzakharchenko.backend.utils.IpUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLConnectionCloseFutureListener implements GenericFutureListener<ChannelFuture> {
    private final Logger LOGGER = LoggerFactory.getLogger(SQLConnectionCloseFutureListener.class);
    private final SQLSession session;

    private final Channel frontend;
    private final Channel backend;

    public SQLConnectionCloseFutureListener(SQLSession session, Channel frontend, Channel backend) {
        this.session = session;
        this.frontend = frontend;
        this.backend = backend;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        Channel ch = future.channel();
        if (ch == frontend) {
            LOGGER.info("frontend channel [{}] closed!", IpUtil.getRemoteAddress(frontend));
            // frontend connection close but it's backend connection is still active or open, close it!
            if (backend.isActive() || backend.isOpen()) {
               // backend.close();
            }
        } else {
            LOGGER.info("backend channel [{}] closed!", IpUtil.getRemoteAddress(backend));
            // backend connection close but it's frontend connection is still active or open, close it!
            if (frontend.isActive() || frontend.isOpen()) {
                frontend.close();
            }
        }
    }
}
