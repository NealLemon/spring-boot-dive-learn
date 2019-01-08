package com.web.webflux.config;

import com.web.webflux.handler.UserHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @ClassName WebFluxRoutingConfiguration
 * @Description 函数式端点
 * @Author Neal
 * @Date 2019/1/8 14:28
 * @Version 1.0
 */
@Configuration
public class WebFluxRoutingConfiguration {

    @Autowired
    private UserHandler userHandler;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route(GET("/webflux/{userId}"), userHandler::getUserById)
                .andRoute(GET("/webflux/users"),userHandler::getAll);
    }

}
