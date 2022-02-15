package com.github.vzakharchenko.backend.frontend.handlers;

import com.github.vzakharchenko.backend.Connector;
import com.github.vzakharchenko.backend.SQLSession;
import com.github.vzakharchenko.backend.mysql.protocol.ErrorPacket;
import com.github.vzakharchenko.backend.mysql.protocol.MySQLAuthorization;
import com.github.vzakharchenko.backend.utils.IpUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Sharable
@Component
public class FrontendConnectionHandler extends ChannelInboundHandlerAdapter implements FrontendChannelHandler {

    public static boolean test = false;

    private static Logger logger = LoggerFactory.getLogger(FrontendConnectionHandler.class);

    private final Connector connector;

    public FrontendConnectionHandler(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SQLSession session = ctx.channel().attr(SQLSession.SQL_SESSION_KEY).get();
        if (session == null) {
            ctx.channel().close();
        } else {
            ErrorPacket.build(3000, cause.getMessage()).write(ctx.channel(), true);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // connect real backend mysql database
        Channel frontend = ctx.channel();
        MySQLAuthorization authorization = new MySQLAuthorization();
        frontend.attr(SQLSession.SQL_CONNECTED_INITED).set(authorization);
        if (FrontendConnectionHandler.test){
            // Send reply
            final WriteListener listener = success -> System.out.println(success ? "reply success" : "reply fail");

            byte[] msg = ByteBufUtil.decodeHexDump(
                    "6d0000000a352e352e352d31302e362e352d4d6172696144422d313a31302e362e352b6d617269617e666f63616c00430000007250782f7a3c5a3200fef72d0200ff81150000000000001d0000002b216044712d2c3154344645006d7973716c5f6e61746976655f70617373776f726400");
            ctx.writeAndFlush(Unpooled.wrappedBuffer(msg)).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (listener != null) {
                        authorization.step1();
                        frontend.attr(SQLSession.SQL_CONNECTED_INITED).set(authorization);
                    //    FrontendConnectionHandler.test=false;
                        listener.messageRespond(future.isSuccess());
                    }
                }
            });
            connector.connectProxy(frontend);
        } else {
            connector.connect("192.100.200.148", 3310, frontend);
        }
    }




    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // directly write frontend data to backend real mysql connection
        SQLSession session = ctx.channel().attr(SQLSession.SQL_SESSION_KEY).get();
        MySQLAuthorization authorization = ctx.channel().attr(SQLSession.SQL_CONNECTED_INITED).get();
        if (FrontendConnectionHandler.test && !authorization.isStep2() ){
            return;
        }
        if (session != null) {
            session.backend().write(msg);
        } else {
            logger.warn("can not found session, so close frontend channel [{}]", IpUtil.getRemoteAddress(ctx.channel()));
            ctx.channel().close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        SQLSession session = ctx.channel().attr(SQLSession.SQL_SESSION_KEY).get();
        MySQLAuthorization authorization = ctx.channel().attr(SQLSession.SQL_CONNECTED_INITED).get();

        if (FrontendConnectionHandler.test && !authorization.isStep2() ){
            authorization.step2();
        }
        if (FrontendConnectionHandler.test && !authorization.isStep3() ){

            // Send reply
            final WriteListener listener = success -> System.out.println(success ? "reply success" : "reply fail");

            byte[] msg0 = ByteBufUtil.decodeHexDump(
                    "0700000200000002000000");
            ctx.writeAndFlush(Unpooled.wrappedBuffer(msg0)).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (listener != null) {
                        authorization.step3();
                        authorization.success();
                        ctx.channel().attr(SQLSession.SQL_CONNECTED_INITED).set(authorization);
                        listener.messageRespond(future.isSuccess());
                    }
                }
            });
            return;
        }
        if (session != null) {
            session.backend().writeAndFlush(Unpooled.EMPTY_BUFFER);
        } else {
            logger.warn("can not found session, so close frontend channel [{}]", IpUtil.getRemoteAddress(ctx.channel()));
            ctx.channel().close();
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public int order() {
        return 1000000;
    }

    public interface WriteListener {
        void messageRespond(boolean success);
    }
}
