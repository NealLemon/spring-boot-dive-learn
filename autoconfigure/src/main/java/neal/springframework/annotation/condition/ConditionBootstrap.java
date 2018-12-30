package neal.springframework.annotation.condition;


import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * 引导类
 */
public class ConditionBootstrap {

    /**
     * @Conditional 可以用于方法级别上
     * 如果 java系统属性 user.name 和Neal 相等，则加载helloWorld
     * @return helloWolrd bean
     */
    @Bean
    @ConditionalOnSystemProperty(name="user.name",value = "Neal")
    public String helloWorld() {
        return "Hello World ";
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ConditionBootstrap.class)
                .web(WebApplicationType.NONE)
                .run(args);

        // helloWorld Bean 是否存在
        String helloWorld =
                context.getBean("helloWorld", String.class);

        System.out.println("helloWorld Bean : " + helloWorld);

        // 关闭上下文
        context.close();
    }
}
