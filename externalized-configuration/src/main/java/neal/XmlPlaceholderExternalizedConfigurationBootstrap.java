package neal;

import neal.externalized.domain.SpringXmlCar;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;


@ImportResource("META-INF/spring/car-context.xml") // 加载 Spring 上下文 XML 文件
@PropertySource("META-INF/default.properties")   //加载配置文件
@EnableAutoConfiguration
public class XmlPlaceholderExternalizedConfigurationBootstrap {

    public static void main(String[] args) {

        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(XmlPlaceholderExternalizedConfigurationBootstrap.class)
                        .web(WebApplicationType.NONE) // 非 Web 应用
                        .run(args);

        SpringXmlCar springXmlCar = context.getBean("springXmlCar", SpringXmlCar.class);
        System.out.println("获取汽车信息 : " + springXmlCar);
        // 关闭上下文
        context.close();
    }
}
