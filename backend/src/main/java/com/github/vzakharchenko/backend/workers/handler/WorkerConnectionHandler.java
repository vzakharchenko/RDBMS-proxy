package com.github.vzakharchenko.backend.workers.handler;

import com.github.vzakharchenko.backend.SQLSession;
import com.github.vzakharchenko.backend.mysql.protocol.ErrorPacket;
import com.github.vzakharchenko.backend.mysql.protocol.MySQLAuthorization;
import com.github.vzakharchenko.backend.utils.IpUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Sharable
@Component
public class WorkerConnectionHandler extends ChannelInboundHandlerAdapter implements WorkerChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerConnectionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // write error packet to frontend
        SQLSession session = ctx.channel().attr(SQLSession.SQL_SESSION_KEY).get();
        if (session != null) {
            Channel frontend = session.frontend();
            ErrorPacket.build(3000, cause.getMessage()).write(frontend, true);
        } else {
            LOGGER.warn("can not find frontend connection of backend [{}]", IpUtil.getAddress(ctx.channel()));
        }
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SQLSession session = ctx.channel().attr(SQLSession.SQL_SESSION_KEY).get();
        Channel frontend = session.frontend();
        frontend.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        SQLSession session = ctx.channel().attr(SQLSession.SQL_SESSION_KEY).get();
        Channel frontend = session.frontend();
        frontend.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public int order() {
        return 1000000;
    }
}
