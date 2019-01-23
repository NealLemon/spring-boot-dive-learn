package neal.externalized.listener;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @ClassName ExternalizeApplicationContextInitializer
 * @Description 自定义上下文初始化实现类
 * @Author Neal
 * @Date 2019/1/23 19:05
 * @Version 1.0
 */
public class ExternalizeApplicationContextInitializer implements ApplicationContextInitializer {


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        //获取environment 对象
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        //获取项目跟路径
        String classpath = ExternalizeApplicationContextInitializer.class.getResource("/").getPath();
        //获取PropertySource组合对象
        MutablePropertySources propertySources = environment.getPropertySources();
        //获取自定义的外部化配置资源
        File file = new File(classpath +"config/applicationinitializer.properties");

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
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---ExternalizeApplicationContextInitializer",properties);
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
}
