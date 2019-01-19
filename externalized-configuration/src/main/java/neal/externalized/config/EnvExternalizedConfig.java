package neal.externalized.config;

import neal.externalized.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class EnvExternalizedConfig {

    //构造方式注入 environment
    private final Environment constructorEnvironment;

    //依赖注入
    @Autowired
    private Environment environment;


    @Autowired
    public EnvExternalizedConfig(Environment environment) {
        this.constructorEnvironment = environment;
    }

    /**
     * 方法注入
     * @param methodEnvironment
     * @return
     */
    @Bean
    public User methodUser(Environment methodEnvironment) {
        User user = new User();
        String name = methodEnvironment.getRequiredProperty("evn.name");
        int age = methodEnvironment.getRequiredProperty("evn.age",Integer.class);
        user.setName(name);
        user.setAge(age);
        return user;
    }

    /**
     * 构造方式注入
     * @return
     */
    @Bean
    public User constructorUser() {
        User user = new User();
        String name = constructorEnvironment.getRequiredProperty("evn.name");
        int age = constructorEnvironment.getRequiredProperty("evn.age",Integer.class);
        user.setName(name);
        user.setAge(age);
        return user;
    }

    /**
     * 依赖注入
     * @return
     */
    @Bean
    public User autowiredUser() {
        User user = new User();
        String name = environment.getRequiredProperty("evn.name");
        int age = environment.getRequiredProperty("evn.age",Integer.class);
        user.setName(name);
        user.setAge(age);
        return user;
    }

}
