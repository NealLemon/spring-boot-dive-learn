package com.web.webflux.handler;

import com.web.webflux.entity.User;
import com.web.webflux.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @ClassName UserHandler
 * @Description TODO
 * @Author Neal
 * @Date 2019/1/8 14:30
 * @Version 1.0
 */
@Component
public class UserHandler {

    @Autowired
    private UserRepository userRepository;

    public Mono<ServerResponse> getUserById(ServerRequest serverRequest) {
        return ServerResponse.status(HttpStatus.OK)
                .body(Mono.just(userRepository.getUserByUserId().get(Integer.valueOf(serverRequest.pathVariable("userId")))), User.class);
    }


    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
        return null;
    }
}
