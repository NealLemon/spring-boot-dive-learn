# Springboot 2.0---WebFlux核心组件的初始化

本文是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。 

## 核心组件介绍

### HttpHandler VS Servlet 

因为Sping 力推 WebFlux 主要是想去Servlet 化，但是很多接口和流程都是模仿Servlet来实现的。我们先从底层API开始看起

####  HttpHandler

```java
public interface HttpHandler {

   /**
    * Handle the given request and write to the response.
    * @param request current request
    * @param response current response
    * @return indicates completion of request handling
    */
   Mono<Void> handle(ServerHttpRequest request, ServerHttpResponse response);

}
```

#### Servlet

```java
public interface Servlet {
    public void init(ServletConfig config) throws ServletException;
    
    //HttpHandler#handle
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException;
    public String getServletInfo();

    public void destroy();
}
```

为了方便观看，我把源码中的注解给注释掉了，那么我们现在对比来看，我们可以看到 HttpHandler中的 `hanlde`方法和 Servlet中的  `service`方法很像。这就是这两种 Framework 最核心的区别 。



## 其他组件介绍

p13.png



这里直接摘取了小马哥课程的内容，在相关的官方文档里也有，这里就不一一解释了，感兴趣的同学可以去官网看文档的详细解释。



## 核心组件初始化流程

  既然说到初始化流程，那么就不能不上流程图了。

p12.png



 我们通过流程图相信已经可以理解百分四五十了,那么接下来让我们来通过源码来简单的解释一下详细流程。



### 源码解析

##### 1.Springboot 启动流程

​	想要理解WebFlux核心组件的初始化流程，我们就不能先理解Springboot的启动流程，这里不多做解释我直接给结果，如果想了解具体启动流程可以参看我之前的记录[《Spring-boot---SpringApplication运行阶段》](https://www.jianshu.com/p/6d4252d6bce4)虽然解释的不是很全面，但是大体上的流程已经解释的很清楚了。

  这里就直接给出到底是在哪一步来加载这些组件的。

  答案就是在 启动过程中 上下文刷新的时候。

```java
public ConfigurableApplicationContext run(String... args) {
//省略部分代码
   try {
//省略部分代码
      prepareContext(context, environment, listeners, applicationArguments,
            printedBanner);
       //--刷新上下文--
      refreshContext(context);
      afterRefresh(context, applicationArguments);
//省略部分代码
   }
//省略部分代码
   return context;
}
```

在注解中我已经标出该方法，那么到底是在刷新上下文的哪一步开始执行的呢 。

`org.springframework.context.support.AbstractApplicationContext#refresh` 是在这个抽象类方法中的 `invokeBeanFactoryPostProcessors(beanFactory);`

```java
@Override
public void refresh() throws BeansException, IllegalStateException {
   synchronized (this.startupShutdownMonitor) {
      // Prepare this context for refreshing.
      prepareRefresh();

      // Tell the subclass to refresh the internal bean factory.
      ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

      // Prepare the bean factory for use in this context.
      prepareBeanFactory(beanFactory);

      try {
         // Allows post-processing of the bean factory in context subclasses.
         postProcessBeanFactory(beanFactory);

         // Invoke factory processors registered as beans in the context.  
          //在上下文中调用 工厂处理类来注册Beans
         invokeBeanFactoryPostProcessors(beanFactory);

         // Register bean processors that intercept bean creation.
         registerBeanPostProcessors(beanFactory);

         // Initialize message source for this context.
         initMessageSource();

         // Initialize event multicaster for this context.
         initApplicationEventMulticaster();

         // Initialize other special beans in specific context subclasses.
         onRefresh();

         // Check for listener beans and register them.
         registerListeners();

         // Instantiate all remaining (non-lazy-init) singletons.
         finishBeanFactoryInitialization(beanFactory);

         // Last step: publish corresponding event.
         finishRefresh();
      }

      catch (BeansException ex) {
         if (logger.isWarnEnabled()) {
            logger.warn("Exception encountered during context initialization - " +
                  "cancelling refresh attempt: " + ex);
         }

         // Destroy already created singletons to avoid dangling resources.
         destroyBeans();

         // Reset 'active' flag.
         cancelRefresh(ex);

         // Propagate exception to caller.
         throw ex;
      }

      finally {
         // Reset common introspection caches in Spring's core, since we
         // might not ever need metadata for singleton beans anymore...
         resetCommonCaches();
      }
   }
}
```



这里就把代码全部贴出来了， 方便大家来根据注释来理解每一步。接下来我们就要知道是哪一个 Bean的工厂处理类来初始化我们的组件。

由于调用复杂的原因，我这里就直接给出这个处理类 `org.springframework.context.annotation.ConfigurationClassPostProcessor ` 在这个处理类中，我们可以处理注册被模式注解装饰的Bean。

通过类中的解析方法 `org.springframework.context.annotation.ConfigurationClassPostProcessor#processConfigBeanDefinitions` 中声明的 `ConfigurationClassParser` 对象来解析 模式注解。