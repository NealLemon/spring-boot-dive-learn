package neal.externalized;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * @ClassName ExternalizedBootstrap
 * @Description 引导类
 * @Author Neal
 * @Date 2019/1/18 11:11
 * @Version 1.0
 */
@EnableAutoConfiguration
@ComponentScan(basePackages = "neal.externalized")  //扫描指定包下的类
public class ExternalizedBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(ExternalizedBootstrap.class)
                        .web(WebApplicationType.NONE) // 非 Web 应用
                        .run(args);

        ConfigurableEnvironment environment = context.getEnvironment();

        System.err.println("测试扩展外部化配置结果: "  + environment.getProperty("test"));

        environment.getPropertySources().forEach(propertySource -> {
            System.err.printf("PropertySource[名称:%s]  :  %s\n",propertySource.getName(),propertySource);
            System.out.println();
        });
        // 关闭上下文
        context.close();
    }
}
