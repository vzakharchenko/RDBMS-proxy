package com.github.vzakharchenko.backend.frontend;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FrontendNioEventLoopGroupImpl extends NioEventLoopGroup implements FrontendNioEventLoopGroup {
    public FrontendNioEventLoopGroupImpl(@Value("${server.sql.frontend.thread.count}") int frThreadCnt) {
        super(frThreadCnt >0 ? frThreadCnt: 1);
    }
}
