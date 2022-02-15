package com.github.vzakharchenko.backend.workers.handler;

import com.github.vzakharchenko.backend.utils.IpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Sharable
@Component
public class WorkerConnectionLogHandler extends ChannelInboundHandlerAdapter implements WorkerChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerConnectionLogHandler.class);

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("on worker channel [{}] registered", IpUtil.getAddress(ctx.channel()));
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("on worker channel [{}] unregistered", IpUtil.getAddress(ctx.channel()));
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("on worker channel [{}] active", IpUtil.getAddress(ctx.channel()));
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("on worker channel [{}] inactive", IpUtil.getAddress(ctx.channel()));
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("on worker channel [{}] read, will write data directly to frontend, data:\r\n {}",
                IpUtil.getAddress(ctx.channel()), ByteBufUtil.prettyHexDump((ByteBuf) msg));
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("on worker channel [{}] readComplete", IpUtil.getAddress(ctx.channel()));
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.info("on worker channel [{}] exception", IpUtil.getAddress(ctx.channel()), cause);
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public int order() {
        return 0;
    }
}
