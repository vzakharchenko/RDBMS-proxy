package com.github.vzakharchenko.backend.workers;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WorkerNioEventLoopGroupImpl extends NioEventLoopGroup implements WorkerNioEventLoopGroup {
    public WorkerNioEventLoopGroupImpl(@Value("${server.sql.backend.thread.count}") int bkThreadCnt) {
        super(bkThreadCnt > 0 ? bkThreadCnt : Runtime.getRuntime().availableProcessors());
    }
}
