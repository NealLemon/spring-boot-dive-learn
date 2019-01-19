package neal;

import neal.externalized.domain.Car;
import neal.externalized.domain.ConstructorCar;
import neal.externalized.domain.MethodCar;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName ValueAnnotationBootstrap
 * @Description
 * @Author Neal
 * @Date 2019/1/18 11:11
 * @Version 1.0
 */

@EnableAutoConfiguration
@ComponentScan(basePackages = "neal.externalized")  //扫描指定包下的类
public class ValueAnnotationBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(ValueAnnotationBootstrap.class)
                        .web(WebApplicationType.NONE) // 非 Web 应用
                        .run(args);

        Car  car = context.getBean("car", Car.class);
        MethodCar methodCar = context.getBean("methodCar",MethodCar.class);
        ConstructorCar constructorCar = context.getBean("constructorCar",ConstructorCar.class);

        System.out.println("@Value字段注入 " + car.toString());

        System.out.println("@Value方法注入 " + methodCar.toString());

        System.out.println("@Value构造注入 " + constructorCar.toString());

        // 关闭上下文
        context.close();
    }
}
