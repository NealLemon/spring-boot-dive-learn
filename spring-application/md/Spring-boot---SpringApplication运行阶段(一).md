# Spring-boot---SpringApplication运行阶段（一）

本文以及所有的spring-boot相关文章都是基于spring-boot 2.x版本。

所谓的运行阶段，也就是`org.springframework.boot.SpringApplication#run(java.lang.String...)`方法的阶段。先看一下源码

```java
/**
 * Run the Spring application, creating and refreshing a new
 * {@link ApplicationContext}.
 * @param args the application arguments (usually passed from a Java main method)
 * @return a running {@link ApplicationContext}
 */
public ConfigurableApplicationContext run(String... args) {
   //1.计时工具
   StopWatch stopWatch = new StopWatch();
   //计时开始
   stopWatch.start();
    
   //声明上下文
   ConfigurableApplicationContext context = null;
   //故障分析集合 
   Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
    
   //2.设置headless模式 就是设置系统属性java.awt.headless 
   configureHeadlessProperty();
    
   //3.使用工厂方法 获取SpringApplicationRunListener 也就是springboot运行时监听 
   SpringApplicationRunListeners listeners = getRunListeners(args);
   
   //使用组合对象的设计模式 迭代的执行starting() 
   listeners.starting();
   try {
      //获取默认的应用参数 
      ApplicationArguments applicationArguments = new DefaultApplicationArguments(
            args);
       //4.创建配置Environment
      ConfigurableEnvironment environment = prepareEnvironment(listeners,
            applicationArguments);
      configureIgnoreBeanInfo(environment);
      //打印Banner 
      Banner printedBanner = printBanner(environment);
      
      //5.创建spring-boot上下文
      context = createApplicationContext();
       
       //获取启动错误报告实例
      exceptionReporters = getSpringFactoriesInstances(
            SpringBootExceptionReporter.class,
            new Class[] { ConfigurableApplicationContext.class }, context);
       
      //6.上下文启动之前准备并装载 
      prepareContext(context, environment, listeners, applicationArguments,
            printedBanner);
       
      //7.刷新上下文 
      refreshContext(context);
       
      //刷新后的上下文处理  
      afterRefresh(context, applicationArguments);
       
      //计时结束
      stopWatch.stop();
       
      //打印日志 
      if (this.logStartupInfo) {
         new StartupInfoLogger(this.mainApplicationClass)
               .logStarted(getApplicationLog(), stopWatch);
      }
       
      //监听spring上下文，此时上下文已启动,Spring Bean已初始化完成
      listeners.started(context);
      callRunners(context, applicationArguments);
   }
   catch (Throwable ex) {
      handleRunFailure(context, ex, exceptionReporters, listeners);
      throw new IllegalStateException(ex);
   }

   //启动运行监听
   try {
      listeners.running(context);
   }
   catch (Throwable ex) {
      handleRunFailure(context, ex, exceptionReporters, null);
      throw new IllegalStateException(ex);
   }
   return context;
}
```

大体的步骤已经在源码中添加了注释。现在需要分步理解。

1.`StopWatch`: 一个简单的计时器，就是记录整体spring-boot加载成功完成的时间。在运行之前声明并开始计时在上下文刷新后结束计时。
2.`configureHeadlessProperty()`:设置headless模式---Headless模式是系统的一种配置模式。在该模式下，系统缺少了显示设备、键盘或鼠标。Headless模式虽然不是我们愿意见到的，但事实上我们却常常需要在该模式下工作，尤其是[服务器端](http://cpro.baidu.com/cpro/ui/uijs.php?adclass=0&app_id=0&c=news&cf=1001&ch=0&di=128&fv=18&is_app=0&jk=c26a301180024181&k=%B7%FE%CE%F1%C6%F7%B6%CB&k0=%B7%FE%CE%F1%C6%F7%B6%CB&kdi0=0&luki=6&mcpm=0&n=10&p=baidu&q=smileking_cpr&rb=0&rs=1&seller_id=1&sid=8141028011306ac2&ssp2=1&stid=9&t=tpclicked3_hc&td=1682280&tu=u1682280&u=http%3A%2F%2Fwww.th7.cn%2FProgram%2Fjava%2F201408%2F256265.shtml&urlid=0)程序开发者。因为服务器（如提供Web服务的[主机](http://cpro.baidu.com/cpro/ui/uijs.php?adclass=0&app_id=0&c=news&cf=1001&ch=0&di=128&fv=18&is_app=0&jk=c26a301180024181&k=%D6%F7%BB%FA&k0=%D6%F7%BB%FA&kdi0=0&luki=5&mcpm=0&n=10&p=baidu&q=smileking_cpr&rb=0&rs=1&seller_id=1&sid=8141028011306ac2&ssp2=1&stid=9&t=tpclicked3_hc&td=1682280&tu=u1682280&u=http%3A%2F%2Fwww.th7.cn%2FProgram%2Fjava%2F201408%2F256265.shtml&urlid=0)）往往可能缺少前述设备，但又需要使用他们提供的功能，生成相应的数据，以提供给客户端（如浏览器所在的配有相关的[显示设备](http://cpro.baidu.com/cpro/ui/uijs.php?adclass=0&app_id=0&c=news&cf=1001&ch=0&di=128&fv=18&is_app=0&jk=c26a301180024181&k=%CF%D4%CA%BE%C9%E8%B1%B8&k0=%CF%D4%CA%BE%C9%E8%B1%B8&kdi0=0&luki=7&mcpm=0&n=10&p=baidu&q=smileking_cpr&rb=0&rs=1&seller_id=1&sid=8141028011306ac2&ssp2=1&stid=9&t=tpclicked3_hc&td=1682280&tu=u1682280&u=http%3A%2F%2Fwww.th7.cn%2FProgram%2Fjava%2F201408%2F256265.shtml&urlid=0)、键盘和[鼠标](http://cpro.baidu.com/cpro/ui/uijs.php?adclass=0&app_id=0&c=news&cf=1001&ch=0&di=128&fv=18&is_app=0&jk=c26a301180024181&k=%CA%F3%B1%EA&k0=%CA%F3%B1%EA&kdi0=0&luki=3&mcpm=0&n=10&p=baidu&q=smileking_cpr&rb=0&rs=1&seller_id=1&sid=8141028011306ac2&ssp2=1&stid=9&t=tpclicked3_hc&td=1682280&tu=u1682280&u=http%3A%2F%2Fwww.th7.cn%2FProgram%2Fjava%2F201408%2F256265.shtml&urlid=0)的主机）。

3.加载SpringApplication运行监听(`SpringApplicationRunListener`):使用Spring工厂机制，加载`SpringApplicationRunListener`对象集合。我们可以从` META-INF/spring.factories`中找到其实现类(`org.springframework.boot.context.event.EventPublishingRunListener`)这里有一个细节，源码如下

```java
private SpringApplicationRunListeners getRunListeners(String[] args) {
   Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
    //生成SpringApplicationRunListeners  SpringApplicationRunListener的对象集合
   return new SpringApplicationRunListeners(logger, getSpringFactoriesInstances(
         SpringApplicationRunListener.class, types, this, args));
}
```

这里使用工厂机制加载`SpringApplicationRunListener`后，生成了 `SpringApplicationRunListeners`的运行监听对象集合的对象。在`SpringApplicationRunListeners`中我们可以看到这个类使用了组合对象的设计模式，来迭代执行`SpringApplicationRunListeners`的对象集合。这里只给出`SpringApplicationRunListeners`构造函数，从构造函数就可以看出这个类中定义了所有实现`SpringApplicationRunListener`接口的集合，具体的其他方法，可以自行查看源码。

```java
SpringApplicationRunListeners(Log log,
      Collection<? extends SpringApplicationRunListener> listeners) {
   this.log = log;
   this.listeners = new ArrayList<>(listeners);
}
```

额外讲一下`SpringApplicationRunListener`的唯一的具体实现`org.springframework.boot.context.event.EventPublishingRunListener`

```java
public class EventPublishingRunListener implements SpringApplicationRunListener, Ordered {

   private final SpringApplication application;

   private final String[] args;

   //使用广播器来广播事件
   private final SimpleApplicationEventMulticaster initialMulticaster;

   public EventPublishingRunListener(SpringApplication application, String[] args) {
      this.application = application;
      this.args = args;
      this.initialMulticaster = new SimpleApplicationEventMulticaster();
      for (ApplicationListener<?> listener : application.getListeners()) {
         this.initialMulticaster.addApplicationListener(listener);
      }
   }
```

这里所有监听的事件都是使用`SimpleApplicationEventMulticaster`来发布的。我们再进一步看一下广播器类的源码

```java
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

   @Nullable
   private Executor taskExecutor;

   @Nullable
   private ErrorHandler errorHandler;
    
    
    	@Override
	public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
		ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
		for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
            //异步执行
			Executor executor = getTaskExecutor();
			if (executor != null) {
				executor.execute(() -> invokeListener(listener, event));
			}
            //同步执行
			else {
				invokeListener(listener, event);
			}
		}
	}
```

在通过广播器关联到相关的监听后，这里会异步或同步的调用监听器。

4.创建并配置`ApplicationConext`的`Environment`：`Environment`是对配置文件(*profiles* )和属性文件(*properties*)两个关键应用环境方面的建模。提供激活和默认的配置文件和 操作底层配置资源的功能。根据SpringApplication准备阶段判断的环境类型来做配置操作。

5.创建ApplicationContext：这里通过判断当前`webApplicationType`的类型来加载对应的上下文。

6.上下文启动之前准备并装载(`prepareContext`)，可以看源码中给出的注释：

```java
private void prepareContext(ConfigurableApplicationContext context,
      ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
      ApplicationArguments applicationArguments, Banner printedBanner) {
    //设置上下文
   context.setEnvironment(environment);
    //注册单例Bean以及资源加载策略（ResourceLoader）
   postProcessApplicationContext(context);
    //初始化所有实现ApplicationContextInitializer 接口的上下文
   applyInitializers(context);
    //发布上下文准备事件
   listeners.contextPrepared(context);
    
   if (this.logStartupInfo) {
      logStartupInfo(context.getParent() == null);
      logStartupProfileInfo(context);
   }

   // Add boot specific singleton beans
   context.getBeanFactory().registerSingleton("springApplicationArguments",
         applicationArguments);
   if (printedBanner != null) {
      context.getBeanFactory().registerSingleton("springBootBanner", printedBanner);
   }

   // Load the sources
   Set<Object> sources = getAllSources();
   Assert.notEmpty(sources, "Sources must not be empty");
   //初始化Bean 完成Bean的加载
   load(context, sources.toArray(new Object[0]));
    
   //发布上下文装载事件
   listeners.contextLoaded(context);
}
```

7.刷新上下文`  refreshContext(context)`:这里只是一个引用方法具体方法在`org.springframework.context.support.AbstractApplicationContext#refresh`方法中,由于刷新阶段需要做很多操作，看注释就可以看明白大体步骤，具体细节可以自己看源码再结合其他前辈的博文理解。

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

#### 总结

SpringApplication运行阶段主要就分为:

- 加载:SpringApplication运行监听器(`SpringApplicationRunListener`)
- 运行:SpringApplication运行监听器(`SpringApplicationRunListeners`)
- 监听:Spring-boot事件，spring事件。
- 创建:创建上下文，`Environment`,其他。
- 失败：打印故障分析报告。
- 回调。



最后附上监听和事件列表