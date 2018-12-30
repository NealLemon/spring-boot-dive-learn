package com.web.configuration;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * Spring 拦截器 配置
 */
@Configuration   //配置
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 添加拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                System.out.println("---------自定义拦截器拦截-------");
                return true;
            }
        });
    }


    /**
     * 解决在IDEA下maven多模块使用spring-boot跳转JSP 404问题
     * @return
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
        return (factory -> {
            factory.addContextCustomizers(context -> {
                //当前webapp路径
                String relativePath = "springboot-webmvc/src/main/webapp";
                File docBaseFile = new File(relativePath);
                if(docBaseFile.exists()) {
                    context.setDocBase(new File(relativePath).getAbsolutePath());
                }
            });
        });
    }

}
