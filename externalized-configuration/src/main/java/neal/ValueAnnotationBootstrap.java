package neal;

import neal.externalized.domain.Car;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName ValueAnnotationBootstrap
 * @Description TODO
 * @Author Neal
 * @Date 2019/1/18 11:11
 * @Version 1.0
 */

@EnableAutoConfiguration
@ComponentScan(basePackages = "neal.externalized")
public class ValueAnnotationBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(ValueAnnotationBootstrap.class)
                        .web(WebApplicationType.NONE) // 非 Web 应用
                        .run(args);

        Car  car = context.getBean("car", Car.class);

        System.out.println(car.toString());

        // 关闭上下文
        context.close();
    }
}
