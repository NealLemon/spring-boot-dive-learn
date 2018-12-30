package neal.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

/**
 * 自定义上下文初始化事件 监听顺序在 {@HelloWorldRefreshedEvent} 之后
 */
public class AfterHelloWorldRefreshedEvent implements ApplicationListener<ContextRefreshedEvent>, Ordered {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("上下文启动完成事件AfterHelloWorldRefreshedEvent:" + event.getApplicationContext().getId() + ",timestamp:" + event.getTimestamp());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE +1;
    }
}
