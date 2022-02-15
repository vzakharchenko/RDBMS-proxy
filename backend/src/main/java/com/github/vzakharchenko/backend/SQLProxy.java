package com.github.vzakharchenko.backend;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SQLProxy implements InitializingBean {

    @Value("${server.sql.port}")
    private int port;

    @Autowired
    private Acceptor acceptor;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.err.println("Proxying *:" + port + " ...");
        acceptor.start();
    }
}
