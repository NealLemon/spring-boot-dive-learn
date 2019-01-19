package neal.externalized.config;

import neal.externalized.domain.Car;
import neal.externalized.domain.ConstructorCar;
import neal.externalized.domain.MethodCar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName ValueAnnotationBootstrap
 * @Description config声明bean
 * @Author Neal
 * @Date 2019/1/18 11:11
 * @Version 1.0
 */

@Configuration
public class ExternalizedConfig {


    private final String name;

    private final String color;

    private final String producer;

    /**
     * 构造器注入
     * @param name
     * @param color
     * @param producer
     */
    public ExternalizedConfig(@Value("${car.name}") String name,
                              @Value("${car.color}") String color,
                              @Value("${car.producer:ConstructorProducer}") String producer) {
        this.name = name;
        this.color = color;
        this.producer = producer;
    }

    @Bean
    public ConstructorCar constructorCar() {
        ConstructorCar constructorCar = new ConstructorCar();
        constructorCar.setName(name);
        constructorCar.setColor(color);
        constructorCar.setProducer(producer);
        return constructorCar;
    }

    /**
     * 字段注入
     * @return
     */
    @Bean
    public Car car() {
        return new Car();
    }


    /**
     *
     * 方法注入
     * @param name
     * @param color
     * @param producer
     * @return
     */
    @Bean
    public MethodCar methodCar(@Value("${car.name}") String name,
                               @Value("${car.color}") String color,
                               @Value("${car.producer:MethodProducer}") String producer) {
        MethodCar methodCar = new MethodCar();
        methodCar.setName(name);
        methodCar.setColor(color);
        methodCar.setProducer(producer);
        return methodCar;
    }
}
