package com.github.vzakharchenko.backend;

import com.github.vzakharchenko.backend.mysql.protocol.MySQLAuthorization;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class SQLSession {
    public static final String SQL_SESSION = "sql_session";
    public static final String SQL_CONNECTED = "sql_connected_inited";
    public static final AttributeKey<SQLSession> SQL_SESSION_KEY = AttributeKey.valueOf(SQL_SESSION);
    public static final AttributeKey<MySQLAuthorization> SQL_CONNECTED_INITED = AttributeKey.valueOf(SQL_CONNECTED);

    private final Channel frontend;
    private final Channel backend;

    private SQLSession(Channel frontend, Channel backend) {
        this.frontend = frontend;
        this.backend = backend;
        backend.attr(SQL_SESSION_KEY).set(this);
        frontend.attr(SQL_SESSION_KEY).set(this);
        SQLConnectionCloseFutureListener futureListener = new SQLConnectionCloseFutureListener(this,
                frontend,
                backend);
        this.frontend.closeFuture().addListener(futureListener);
       this.backend.closeFuture().addListener(futureListener);
    }

    public Channel backend() {
        return this.backend;
    }

    public Channel frontend() {
        return this.frontend;
    }

    public static void startSession(Channel frontend, Channel backend) {
        new SQLSession(frontend, backend);
    }

}
