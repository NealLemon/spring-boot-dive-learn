package com.web.webflux.config;

import com.web.webflux.handler.UserHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class WebFluxRoutingConfig {

    @Autowired
    private UserHandler userHandler;

    @Bean("routerFunction1")
    public RouterFunction<ServerResponse> routerFunction() {
        return route(GET("/webflux/user1/{userId}"), userHandler::getUserById)
                .andRoute(GET("/webflux/user1s"),userHandler::getAll);
    }
}
