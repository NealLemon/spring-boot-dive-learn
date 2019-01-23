package neal.externalized.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.*;
import java.util.Properties;

/**
 * 扩展 {@link PropertySource}
 */
public class ExternalizePropertyListener implements SpringApplicationRunListener,Ordered {

    private final SpringApplication application;

    private final String[] args;

    public ExternalizePropertyListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void starting() {

    }

    /**
     * 扩展外部化资源
     * @param environment
     */
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {

        //获取项目跟路径
        String classpath = ExternalizePropertyListener.class.getResource("/").getPath();
        //获取PropertySource组合对象
        MutablePropertySources propertySources = environment.getPropertySources();

        //获取自定义的外部化配置资源
        File file = new File(classpath +"config/externalizepropertylistener.properties");
        /**
         * 获取Property对象
         */
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            //声明一个properties对象
            Properties properties = new Properties();
            // 加载字符流成为 Properties 对象
            properties.load(reader);
            //声明Spring内置PropertiesPropertySource对象
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---ExternalizePropertyListener",properties);
            //将配置资源放到其他配置资源的首位
            propertySources.addFirst(propertySource);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void started(ConfigurableApplicationContext context) {

    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }
    //加载顺序在EventPublishingRunListener之前
    // 这么做是为了之后的外部化配置展示
    @Override
    public int getOrder() {
        return -1;
    }
}
