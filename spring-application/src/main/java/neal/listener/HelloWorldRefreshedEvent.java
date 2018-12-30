package neal.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 自定义上下文初始化事件
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HelloWorldRefreshedEvent implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("上下文启动完成事件HelloWorldRefreshedEvent:" + event.getApplicationContext().getId() + ",timestamp:" + event.getTimestamp());
    }
}
