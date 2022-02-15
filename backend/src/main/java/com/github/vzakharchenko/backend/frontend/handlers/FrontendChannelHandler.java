package com.github.vzakharchenko.backend.frontend.handlers;

import io.netty.channel.ChannelInboundHandler;

public interface FrontendChannelHandler extends ChannelInboundHandler {
    int order();
}
