package com.web.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring Mvc 配置
 */
@Configuration   //配置
@EnableWebMvc    //激活组件并自动装配
@ComponentScan(basePackages = "com.web")
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     *     <bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
     *         <property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/>
     *         <property name="prefix" value="/WEB-INF/jsp/"/>
     *         <property name="suffix" value=".jsp"/>
     *     </bean>
     * @return
     */
    @Bean
    public ViewResolver viewResolver() {

        InternalResourceViewResolver internalResourceViewResolver = new InternalResourceViewResolver();
        internalResourceViewResolver.setViewClass(JstlView.class);
        internalResourceViewResolver.setPrefix("/WEB-INF/jsp/");
        internalResourceViewResolver.setSuffix(".jsp");
        return internalResourceViewResolver;
    }


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

}
