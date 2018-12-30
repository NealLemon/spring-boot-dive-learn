# Spring-boot---SpringApplication准备阶段的加载(五)

## 监听Spring-boot的事件

学习笔记是学习了 小马哥在慕课网的 《Spring Boot 2.0深度实践之核心技术篇》根据自己的需要和理解做的笔记。

### SpringApplication准备阶段加载应用上下文的监听

在`SpringApplication`构造函数的初始化也就是准备阶段，Spring-boot加载装备了所有实现`org.springframework.context.ApplicationListener` 接口的类的实例。如下：

```properties
# Application Listeners
org.springframework.context.ApplicationListener=\
org.springframework.boot.ClearCachesApplicationListener,\
org.springframework.boot.builder.ParentContextCloserApplicationListener,\
org.springframework.boot.context.FileEncodingApplicationListener,\
org.springframework.boot.context.config.AnsiOutputApplicationListener,\
org.springframework.boot.context.config.ConfigFileApplicationListener,\
org.springframework.boot.context.config.DelegatingApplicationListener,\
org.springframework.boot.context.logging.ClasspathLoggingApplicationListener,\
org.springframework.boot.context.logging.LoggingApplicationListener,\
org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener
```

 在这里需要重点说明一下 我们要关注一下这些上下文应用的监听的顺序，也就是`Ordered`接口或者`@Order`注解，然后就是注意每一个监听是监听哪种事件的。我们拿`LoggingApplicationListener`来举例说明。

`LoggingApplicationListener`监听的事件

```java
private static final Class<?>[] EVENT_TYPES = { ApplicationStartingEvent.class,
      ApplicationEnvironmentPreparedEvent.class, ApplicationPreparedEvent.class,
      ContextClosedEvent.class, ApplicationFailedEvent.class };
```

`LoggingApplicationListener`顺序

```java
	/**
	 * The default order for the LoggingApplicationListener.
	 */
	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 20;

	private int order = DEFAULT_ORDER;


@Override
public int getOrder() {
   return this.order;
}
```

我们可以看出如果在`Ordered.HIGHEST_PRECEDENCE + 20`之前加载的所有类都无法打印日志。



SpringApplication运行阶段监听

这里Spring-boot只有唯一一个实现`org.springframework.boot.SpringApplicationRunListener`接口的实现类`org.springframework.boot.context.event.EventPublishingRunListener`通过广播器把事件发送给上下文监听器，也就是我们之间加载的`ApplicationListener`的所有实现类。核心代码如下:

`org.springframework.boot.context.event.EventPublishingRunListener`构造函数，将上下文所有的监听器加载到广播器中。

```java
public EventPublishingRunListener(SpringApplication application, String[] args) {
   this.application = application;
   this.args = args;
   this.initialMulticaster = new SimpleApplicationEventMulticaster();
   for (ApplicationListener<?> listener : application.getListeners()) {
      this.initialMulticaster.addApplicationListener(listener);
   }
```

然后通过`org.springframework.context.event.SimpleApplicationEventMulticaster#multicastEvent()`监听器逐一的发布事件给上文监听器。

```java
@Override
public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
   ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
    //逐一发布监听
   for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
      Executor executor = getTaskExecutor();
      if (executor != null) {
         executor.execute(() -> invokeListener(listener, event));
      }
      else {
         invokeListener(listener, event);
      }
   }
```

过程很简单，但是里面有很多细节判断，需要自己去慢慢了解。



自定义运行监听

让我们实现一下如何监听Spring-boot事件,在默认的监听器中有一个`org.springframework.boot.context.config.ConfigFileApplicationListener`是加载`application.properties` 和 `application.yml` 文件的监听,通过查看源码就可以看到

```java
//在监ApplicationEnvironmentPreparedEvent和ApplicationPreparedEvent
//事件的时候加载 application.properties和 application.yml的文件
@Override
public void onApplicationEvent(ApplicationEvent event) {
   if (event instanceof ApplicationEnvironmentPreparedEvent) {
      onApplicationEnvironmentPreparedEvent(
            (ApplicationEnvironmentPreparedEvent) event);
   }
   if (event instanceof ApplicationPreparedEvent) {
      onApplicationPreparedEvent(event);
   }
}
```



我们现在可以模仿这个实现类，将加载顺序提前于他，那么我们在启动时，是无法从自定义的监听中获取到`application.properties`文件的内容。现在就让我们开始吧。

首先让我们在本地目录下创建`application.properties`文件，并向其中加入KEY VALUE 值。

监听目录.png



```properties
loaded=加载成功
```





自定义`BeforeConfigFileApplicationListener` 代码如下

```java
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
        //因为ConfigFileApplicationListener 的顺序是 Ordered.HIGHEST_PRECEDENCE + 10 
        //所以想要提前就比之前小1就可以了
        return  Ordered.HIGHEST_PRECEDENCE + 9;
    }
}
```

然后把监听的全限定名加入到 自己的 `META-INF/spring.factories`文件中

```properties
# Application Listeners
org.springframework.context.ApplicationListener=\
neal.listener.BeforeConfigFileApplicationListener
```



## 运行

我们来运行下程序看看结果

监听结果.png



我们可以发现KEY为loaded的值时NULL。说明`ConfigFileApplicationListener`装载之前，确实无法获取`application.properties`的内容。

现在让我们改一下顺序，将顺序改为 `Ordered.HIGHEST_PRECEDENCE + 11`,再运行一下程序，查看结果

监听结果2.png



由于编码问题，导致值是乱码，但是不影响我们的结论，确实加载到了值。



### 结论

在学习spring-boot监听机制的时候，一定要明白默认监听的加载顺序以及监听事件类型，方便我们自定义扩展。

