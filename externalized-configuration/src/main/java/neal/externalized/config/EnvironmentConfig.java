package neal.externalized.config;

import neal.externalized.domain.User;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 *  EnvironmentAware,BeanFactoryAware 两种方式获取
 */
@Configuration
public class EnvironmentConfig implements EnvironmentAware,BeanFactoryAware {

    private Environment beanFactoryEnvironment;

    private Environment environment;

    /**
     *  BeanFactoryAware
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactoryEnvironment = beanFactory.getBean(Environment.class);

    }

    /**
     * EnvironmentAware
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     *  BeanFactoryAware 获取的 environment对象初始化
     * @return
     */
    @Bean
    public User beanFactoryAwareUser() {
        User user = new User();
        String name = beanFactoryEnvironment.getRequiredProperty("evn.name");
        int age = beanFactoryEnvironment.getRequiredProperty("evn.age",Integer.class);
        user.setName(name);
        user.setAge(age);
        return user;
    }

    /**
     * EnvironmentAware 获取的environment对象初始化
     * @return
     */
    @Bean
    public User environmentAwareUser() {
        User user = new User();
        String name = environment.getRequiredProperty("evn.name");
        int age = environment.getRequiredProperty("evn.age",Integer.class);
        user.setName(name);
        user.setAge(age);
        return user;
    }

}
