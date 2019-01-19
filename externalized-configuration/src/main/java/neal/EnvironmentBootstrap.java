package neal;

import neal.externalized.domain.User;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName EnvironmentBootstrap
 * @Description
 * @Author Neal
 * @Date 2019/1/18 11:11
 * @Version 1.0
 */

@EnableAutoConfiguration
@ComponentScan(basePackages = "neal.externalized")  //扫描指定包下的类
public class EnvironmentBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(EnvironmentBootstrap.class)
                        .web(WebApplicationType.NONE) // 非 Web 应用
                        .run(args);
        //方法注入
        User methodUser = context.getBean("methodUser", User.class);
        //构造注入
        User constructorUser = context.getBean("constructorUser",User.class);
        //autowired注入
        User autowiredUser = context.getBean("autowiredUser",User.class);

        //BeanFactoryAware接口获取
        User beanFactoryAwareUser = context.getBean("beanFactoryAwareUser",User.class);
        //EnvironmentAware接口获取
        User environmentAwareUser = context.getBean("environmentAwareUser",User.class);

        System.out.println("Environment 方法注入 " + methodUser.toString());

        System.out.println("Environment 构造注入 " + constructorUser.toString());

        System.out.println("Environment 依赖注入 " + autowiredUser.toString());


        System.out.println("Environment BeanFactoryAware接口获取并注入 " + beanFactoryAwareUser.toString());

        System.out.println("Environment EnvironmentAware接口获取并注入 " + environmentAwareUser.toString());

        // 关闭上下文
        context.close();
    }
}
