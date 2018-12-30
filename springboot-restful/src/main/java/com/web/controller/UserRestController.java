package com.web.controller;

import com.web.domain.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRestController {


    //最终相应回浏览器的Content-Type 是 produces 的内容
    @PostMapping(value = "/user",
            consumes = "application/json;charset=UTF-8",
            produces = "application/json;charset=GBK")
    public User user(@RequestBody User user) {
        return user;
    }
}
