package com.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Properties;


/**
 * 自定义实现 Content-Type 为 text/properties
 */
@Controller
public class PropertiesController {

    /**
     * 接受并返回传入的properties
     * @param properties
     * @return
     */
    @PostMapping(value = "/self/properties",
    consumes = "text/properties;charset=UTF-8")
    public Properties resolveProperties(Properties properties) {
        return properties;
    }
}
