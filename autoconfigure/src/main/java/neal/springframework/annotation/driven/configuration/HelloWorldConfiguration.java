package neal.springframework.annotation.driven.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Configuration 注解的解释
 * Indicates that a class declares one or more {@link org.springframework.context.annotation.Bean @Bean} methods and
 * may be processed by the Spring container to generate bean definitions and
 * service requests for those beans at runtime
 * 声明一个@EnableHelloWorldServer 注解的驱动
 */
@Configuration
public class HelloWorldConfiguration {

    @Bean
    public String helloWorld() {
        return "Hello World by HelloWorldConfiguration";
    }
}
