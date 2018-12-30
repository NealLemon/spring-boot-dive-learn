package com.web.configuration;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.io.File;

/**
 * 视图协商相关配置
 */
@Configuration   //配置
public class WebMvcConfig implements WebMvcConfigurer {
    @Bean
    public ViewResolver myViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        //视图解析器顺序
        viewResolver.setOrder(Ordered.LOWEST_PRECEDENCE-10);
        //配置视图解析器的ContentType
        viewResolver.setContentType("html/xml;charset=UTF-8");
        return viewResolver;
    }

    /**
     * 配置协商方法
     */
/*    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorParameter(true).favorPathExtension(true);

    }*/
    /**
     * 解决在IDEA下maven多模块使用spring-boot跳转JSP 404问题
     * @return
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
        return (factory -> {
            factory.addContextCustomizers(context -> {
                //当前webapp路径
                String relativePath = "springboot-restful/src/main/webapp";
                File docBaseFile = new File(relativePath);
                if(docBaseFile.exists()) {
                    context.setDocBase(new File(relativePath).getAbsolutePath());
                }
            });
        });
    }

}
