package neal.externalized.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * EnvironmentPostProcessor 方式实现外部化配置
 */
public class ExternalizePropertyPostProcessor implements EnvironmentPostProcessor,Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        //获取PropertySource组合对象
        MutablePropertySources propertySources = environment.getPropertySources();

        //获取项目跟路径
        String classpath = ExternalizePropertyPostProcessor.class.getResource("/").getPath();

        //获取自定义的外部化配置资源
        File file = new File(classpath +"config/environmentpostprocessor.properties");

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
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---ExternalizePropertyPostProcessor",properties);
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
    public int getOrder() {
        //设置执行顺序在 ConfigFileApplicationListener之后
        //也就是 加载application.properties 之前
        return ConfigFileApplicationListener.DEFAULT_ORDER  - 1;
    }
}
