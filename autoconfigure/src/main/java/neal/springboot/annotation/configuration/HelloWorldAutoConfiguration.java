package neal.springboot.annotation.configuration;

import neal.springframework.annotation.condition.ConditionalOnSystemProperty;
import neal.springframework.annotation.interfaceprograme.annotation.EnableHelloWorldServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HelloWorld 自动装配
 * @Configuration 注解的解释
 * Indicates that a class declares one or more {@link Bean @Bean} methods and
 * may be processed by the Spring container to generate bean definitions and
 * service requests for those beans at runtime
 * 声明一个@EnableHelloWorldServer 注解的驱动
 */
@Configuration
@EnableHelloWorldServer  //spring @Enable 模块装配
@ConditionalOnSystemProperty(name="user.name",value = "Neal")  //条件装配
public class HelloWorldAutoConfiguration {

    @Bean
    public String helloWorld() {
        return "Hello World by HelloWorldConfiguration";
    }
}
