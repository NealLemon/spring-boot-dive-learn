package neal.listener;


import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.Ordered;

/**
 * 加载在默认{@link ConfigFileApplicationListener} 之前
 */
public class BeforeConfigFileApplicationListener implements  SmartApplicationListener, Ordered {

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(eventType)
                || ApplicationPreparedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public boolean supportsSourceType(Class<?> aClass) {
        return true;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            ApplicationEnvironmentPreparedEvent Preparedevent = (ApplicationEnvironmentPreparedEvent) event;
            System.out.println("读取application.properties    loaded="+Preparedevent.getEnvironment().getProperty("loaded"));
        }
        if (event instanceof ApplicationPreparedEvent) {

        }
    }

    @Override
    public int getOrder() {
        return  Ordered.HIGHEST_PRECEDENCE + 11;
    }
}
