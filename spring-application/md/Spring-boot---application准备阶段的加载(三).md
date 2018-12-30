# Spring-boot---application准备阶段的加载(三)

学习笔记是学习了 小马哥在慕课网的 《Spring Boot 2.0深度实践之核心技术篇》根据自己的需要和理解做的笔记。

## 加载事件监听器（ ApplicationListener ）

加载监听器的流程和机制和加载应用上下文初始器`ApplicationContextInitializer`一样。可以参考

[Spring-boot---application准备阶段的加载(一)](https://www.jianshu.com/p/f48db2228a05)

## 应用事件监听器(ApplicationContextInitializer)

### 应用事件标记接口(ApplicationEvent) 

`ApplicationEvent` 继承了JDK自身的标记事件接口`EventObject` ,这个接口是所有事件的一个源而`ApplicationEvent` 是spring的所有应用事件的标记接口。

```java
public abstract class ApplicationEvent extends EventObject {

   /** use serialVersionUID from Spring 1.2 for interoperability */
   private static final long serialVersionUID = 7099057708183571937L;

   /** System time when the event happened */
   private final long timestamp;


   /**
    * Create a new ApplicationEvent.
    * @param source the object on which the event initially occurred (never {@code null})
    */
   public ApplicationEvent(Object source) {
      super(source);
      this.timestamp = System.currentTimeMillis();
   }


   /**
    * Return the system time in milliseconds when the event happened.
    */
   public final long getTimestamp() {
      return this.timestamp;
   }

}
```

源码如上，只记录当前事件的源`source`和发生的时间`timestamp`。



接下来让我们看一下spring的几个上下文事件（ApplicationContextEvent）。

- ContextClosedEvent 上下文关闭事件
- ContextRefreshedEvent 上下文初始化或刷新事件
- ContextStartedEvent 上下文启动事件
- ContextStoppedEvent 上下文停止事件

感兴趣的可以自行查看相关文档或源码。

### 自定义应用事件监听器

接下来我们自定义两个监听事件，来实践一下。监听`ContextRefreshedEvent `事件。在实现自定义`ContextRefreshedEvent ` 事件监听时，我写了两个类，分别实现了`@Order`注解以及`Ordered`接口，来控制加载的顺序。

1.上下文初始化事件

```java
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
```



```java
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
```



### 配置资源文件(spring.factories)

简单的自定义配置类已经写完了,现在让我们把这些类加到spring-boot配置文件中,同样我们需要在本地资源目录(resources)下创建`META-INF/spring.factories`文件。我的目录如图所示：

目录.png

将想要添加的监听的类的全限定名写进`spring.factories`

```properties
# Application Listeners
org.springframework.context.ApplicationListener=\
neal.listener.HelloWorldRefreshedEvent,\
neal.listener.AfterHelloWorldRefreshedEvent
```



### 启动spring-boot

接下来只需要启动spring-boot查看结果是否自定义的监听类是否有监听到。

结果2.png



通过结果可以看到，尽管触发事件的时间相同，但是监听的顺序是正确的。



### 结论

  我们已经简单实现了spring-boot上下文监听事件，因此我们使用自定义监听做很多功能实现，比如监听消息队列等。



[DEMO地址](https://github.com/NealLemon/spring-boot-dive-learn/tree/master/spring-application)

