package com.web.webflux.controller;

import com.web.webflux.entity.User;
import com.web.webflux.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @ClassName WebFluxAnnotatedController
 * @Description
 * @Author Neal
 * @Date 2019/1/8 10:17
 * @Version 1.0
 */
@RestController
@RequestMapping("/annotated/")
public class WebFluxAnnotatedController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 查询单个用户
     * @param id
     * @return  返回Mono 非阻塞单个结果
     */
    @GetMapping("user/{id}")
    public Mono<User> getUserByUserId(@PathVariable("id") int id) {
        printlnThread("获取单个用户");
        return Mono.just(userRepository.getUserByUserId().get(id));
    }

    /**
     *
     * @return  返回Flux 非阻塞序列
     */
    @GetMapping("users")
    public Flux<User> getAll() {
        printlnThread("获取所有用户");
        //使用lambda表达式
        return Flux.fromStream(userRepository.getUsers().entrySet().stream().map(Map.Entry::getValue));
    }

    /**
     * 打印当前线程
     * @param object
     */
    private void printlnThread(Object object) {
        String threadName = Thread.currentThread().getName();
        System.out.println("HelloWorldAsyncController[" + threadName + "]: " + object);
    }
}
