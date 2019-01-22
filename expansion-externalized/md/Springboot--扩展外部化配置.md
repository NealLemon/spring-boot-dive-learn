# Springboot--扩展外部化配置(一)

  笔记是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。 

## 前言

​	在之间介绍过基于SpringFramework以及Springboot如何进行外部化配置,那么现在就让我们来看一下如果去扩展外部化配置。其实在学完这部分内容之后，我就在思考，到底是什么时候才会用到扩展外部化配置，在小马哥的例子中，只是清楚了如何扩展，但是实际的运用场景，由于实际工作的技术限制，目前我还没有接触到。不过了解之后，遇到配置问题，我相信这种扩展外部化配置会排上用场。

## Environment生命周期

### Springboot中的Environment生命周期

  论起生命周期，那么就必须从SpringBoot启动加载开始，我们先回来SpringBoot的启动方法也就是 `org.springframework.boot.SpringApplication#run(java.lang.String...)`

```java
/**
 * Run the Spring application, creating and refreshing a new
 * {@link ApplicationContext}.
 * @param args the application arguments (usually passed from a Java main method)
 * @return a running {@link ApplicationContext}
 */
public ConfigurableApplicationContext run(String... args) {
   StopWatch stopWatch = new StopWatch();
   stopWatch.start();
   ConfigurableApplicationContext context = null;
   Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
   configureHeadlessProperty();
   SpringApplicationRunListeners listeners = getRunListeners(args);
   listeners.starting();
   try {
      ApplicationArguments applicationArguments = new DefaultApplicationArguments(
            args);
       
      // tab 1 
      ConfigurableEnvironment environment = prepareEnvironment(listeners,
            applicationArguments);
      configureIgnoreBeanInfo(environment);
      Banner printedBanner = printBanner(environment);
      context = createApplicationContext();
      exceptionReporters = getSpringFactoriesInstances(
            SpringBootExceptionReporter.class,
            new Class[] { ConfigurableApplicationContext.class }, context);
      //tab 2
      prepareContext(context, environment, listeners, applicationArguments,
            printedBanner);
      refreshContext(context);
      afterRefresh(context, applicationArguments);
      stopWatch.stop();
      if (this.logStartupInfo) {
         new StartupInfoLogger(this.mainApplicationClass)
               .logStarted(getApplicationLog(), stopWatch);
      }
      listeners.started(context);
      callRunners(context, applicationArguments);
   }
   catch (Throwable ex) {
      handleRunFailure(context, ex, exceptionReporters, listeners);
      throw new IllegalStateException(ex);
   }

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

上面这段代码我们在熟悉不过了。在最初就有过简单介绍，我们这只是看 Environment 声明以及之后的调用部分。

我们看上面注释标注的`tab 1`  的地方，我们可以看到在上下文没有启动之前，我们就开始声明`ConfigurableEnvironment`了。我们来看一下内部的方法。



```java
private ConfigurableEnvironment prepareEnvironment(
      SpringApplicationRunListeners listeners,
      ApplicationArguments applicationArguments) {
   //创建 ConfigurableEnvironment 对象 
   //如果是 Servlet容器则返回 StandardServletEnvironment 对象
   //如果是 其他的类型 则返回 StandardEnvironment 对象
   ConfigurableEnvironment environment = getOrCreateEnvironment();
    //配置environment 
   configureEnvironment(environment, applicationArguments.getSourceArgs());
   //监听 environmentPrepared 事件
   listeners.environmentPrepared(environment);
   bindToSpringApplication(environment);
   if (this.webApplicationType == WebApplicationType.NONE) {
      environment = new EnvironmentConverter(getClassLoader())
            .convertToStandardEnvironmentIfNecessary(environment);
   }
   ConfigurationPropertySources.attach(environment);
   return environment;
}
```

 

在上面这段源码中，我们可以重点看一下 `SpringApplicationRunListeners` 的监听`environmentPrepared` 事件。

我们通过进一步源码跟进可以看到 

```java
public void environmentPrepared(ConfigurableEnvironment environment) {
   for (SpringApplicationRunListener listener : this.listeners) {
      listener.environmentPrepared(environment);
   }
}
```

`SpringApplicationRunListeners`  目前只有一个实现类`EventPublishingRunListener`  我们看一下这个实现类中的 `environmentPrepared`方法。

```java
@Override
public void environmentPrepared(ConfigurableEnvironment environment) {
   this.initialMulticaster.multicastEvent(new ApplicationEnvironmentPreparedEvent(
         this.application, this.args, environment));
}
```

我们可以看到这个方法中 使用事件广播器发布了 一个`ApplicationEnvironmentPreparedEvent`事件。



可能说到这，大家觉得很迷惑，为什么要重点讲这一部分呢，其实这一部分跟我们接下来要讲的扩展外部化有很大的关系，所以在这里大家只要记住这个点就行，往后看就会明白为什么重点关注这一部分了。



接下来继续了解 `Environment`生命周期,我们创建完声明周期后，就进入到了 `tab 2` (请看第一部分源码的注释)。

```java
private void prepareContext(ConfigurableApplicationContext context,
      ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
      ApplicationArguments applicationArguments, Banner printedBanner) {
   //将environment 注入到上下文上
   context.setEnvironment(environment);
   postProcessApplicationContext(context);
   //在刷新之前，将任何实现ApplicationContextInitializer接口的类 应用于上下文。
   applyInitializers(context);
   //监听 contextPrepared 事件
   listeners.contextPrepared(context);
   if (this.logStartupInfo) {
      logStartupInfo(context.getParent() == null);
      logStartupProfileInfo(context);
   }

   context.getBeanFactory().registerSingleton("springApplicationArguments",
         applicationArguments);
   if (printedBanner != null) {
      context.getBeanFactory().registerSingleton("springBootBanner", printedBanner);
   }

   Set<Object> sources = getAllSources();
   Assert.notEmpty(sources, "Sources must not be empty");
   load(context, sources.toArray(new Object[0]));
    //监听 contextLoaded 事件
   listeners.contextLoaded(context);
}
```

我直接贴出了内部实现方法。在这里 我贴注释的方法大家都可以留意一下，为什么呢，跟上面的解释一样，扩展外部化配置就是在这些方法或者事件监听的基础上进行的。



接下来的就是刷新上下文，会在其他初始化条件中用到我们的environments。这里就不多做描述。

### 结论

  我们可以看到，我们在扩展外部化配置的时候，一定要遵循一个原则，就是在刷新上下文之前将外部化配置设置好，这样在我们启动容器的过程中，资源加载才不会因为顺序问题或者重复等问题出错。这里直接给出结论，建议在这些步骤之前去扩展外部化配置。

- Spring Framework

  - `org.springframework.context.support.AbstractApplicationContext#prepareBeanFactory `
- SprinBoot
  - `org.springframework.boot.SpringApplication#refreshContext(context)`



## 小结

   这部分的内容，虽然是概念但是非常重要，因为我在看完小马哥后续的扩展实例的视频后，才注意到上面所讲的执行顺序不同，那么他们实现的结果也不相同。接下来的笔记会进入实践环节，扩展外部化实践的方式很多，我们要选择合适的一种就可以。