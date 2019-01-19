package neal.externalized.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用于类注解配置
 */
@ConfigurationProperties(prefix = "configuser")
public class ConfigUser {

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "ConfigUser{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
