package neal.springframework.annotation.interfaceprograme.annotation;


import neal.springframework.annotation.interfaceprograme.selector.HelloWorldSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 基于注解实现的方式声明一个@Enable注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(HelloWorldSelector.class)
public @interface EnableHelloWorldServer {
}
