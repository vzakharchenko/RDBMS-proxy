package com.github.vzakharchenko.backend;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationBackend {

    @Bean
    public Bootstrap bootstrap() {
        return new Bootstrap();
    }

    @Bean
    public ServerBootstrap serverBootstrap() {
        return new ServerBootstrap();
    }
}
