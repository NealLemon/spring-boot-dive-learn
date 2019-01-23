package neal.externalized.listener;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 扩展外部化配置
 */
public class ExternalizePropertyEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    /**
     * 监听到内容时触发的方法
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        //获取项目跟路径
        String classpath = ExternalizePropertyListener.class.getResource("/").getPath();
        //获取PropertySource组合对象
        MutablePropertySources propertySources = event.getEnvironment().getPropertySources();
        //获取自定义的外部化配置资源
        File file = new File(classpath +"config/externalizepropertyeventlistener.properties");

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
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---ApplicationEnvironmentPreparedEvent",properties);
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
