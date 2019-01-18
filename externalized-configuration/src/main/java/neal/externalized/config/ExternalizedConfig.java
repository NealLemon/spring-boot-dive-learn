package neal.externalized.config;

import neal.externalized.domain.Car;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName ValueAnnotationBootstrap
 * @Description TODO
 * @Author Neal
 * @Date 2019/1/18 11:11
 * @Version 1.0
 */

@Configuration
public class ExternalizedConfig {

    @Bean
    public Car car() {
        return new Car();
    }
}
