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
        String classpath = ExternalizePropertyListener.class.getResource("/").getPath();
        MutablePropertySources propertySources = environment.getPropertySources();
        File file = new File(classpath +"config/externalizepropertylistener.properties");
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            //声明一个properties对象
            Properties properties = new Properties();
            // 加载字符流成为 Properties 对象
            properties.load(reader);
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---ExternalizePropertyListener",properties);
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

    @Override
    public int getOrder() {
        return -1;
    }
}
