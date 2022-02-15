package com.github.vzakharchenko.backend.workers.handler;

import io.netty.channel.ChannelInboundHandler;

public interface WorkerChannelHandler extends ChannelInboundHandler {
    int order();
}
