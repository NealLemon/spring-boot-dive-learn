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

public class ExternalizePropertyEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String classpath = ExternalizePropertyListener.class.getResource("/").getPath();
        MutablePropertySources propertySources = event.getEnvironment().getPropertySources();
        File file = new File(classpath +"config/externalizepropertyeventlistener.properties");
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            //声明一个properties对象
            Properties properties = new Properties();
            // 加载字符流成为 Properties 对象
            properties.load(reader);
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---ApplicationEnvironmentPreparedEvent",properties);
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
