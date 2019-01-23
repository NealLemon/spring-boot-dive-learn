package com.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 简单controller
 */
@Controller
public class HelloWorldController {
    @RequestMapping("/index")
    public String index() {
        System.out.println("执行HelloWorldController中的index()方法");
        return "index";
    }



}
