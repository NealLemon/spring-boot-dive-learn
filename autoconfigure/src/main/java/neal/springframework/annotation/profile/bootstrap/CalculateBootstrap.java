package neal.springframework.annotation.profile.bootstrap;

import neal.springframework.annotation.profile.service.CalculateService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 引导类
 */
@SpringBootApplication(scanBasePackages = "neal.springframework.annotation.profile.service")
public class CalculateBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(CalculateBootstrap.class)
                .web(WebApplicationType.NONE)
                .profiles("java8")    //选择使用哪个计算服务
                .run(args);

        // helloWorld Bean 是否存在
        CalculateService calculateService =
                context.getBean(CalculateService.class);

        System.out.println("计算1~10相加: " + calculateService.sum(1,2,3,4,5,6,7,8,9,10));

        // 关闭上下文
        context.close();
    }
}
