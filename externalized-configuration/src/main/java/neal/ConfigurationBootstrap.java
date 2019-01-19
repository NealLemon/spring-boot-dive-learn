package neal;

import neal.externalized.domain.*;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

/**
 * @ClassName ConfigurationBootstrap
 * @Description
 * @Author Neal
 * @Date 2019/1/18 11:11
 * @Version 1.0
 */
@EnableAutoConfiguration
@ComponentScan(basePackages = "neal.externalized")  //扫描指定包下的类
@EnableConfigurationProperties(ConfigUser.class)
public class ConfigurationBootstrap {

    @Bean
    @ConfigurationProperties(prefix = "province")  //方法级别的注入
    //当application.properties 中的province.city.name 必须存在并且其值必须是 Dalian
    @ConditionalOnProperty(name="province.city.name",matchIfMissing = false,havingValue = "Dalian")
    public Province province() {
        return new Province();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(ConfigurationBootstrap.class)
                        .web(WebApplicationType.NONE) // 非 Web 应用
                        .run(args);

        ConfigUser configUser  = context.getBean(ConfigUser.class);
        Province province = context.getBean("province",Province.class);
        System.out.println("@ConfigurationProperties --类注入" + configUser.toString());
        System.out.println("@ConfigurationProperties --方法注入" + province.toString());

        // 关闭上下文
        context.close();
    }
}
