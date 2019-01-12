# Springboot 2.0---WebFlux核心组件的初始化（一）

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



## Springboot 启动流程组件装配源码解析

​    想要理解WebFlux核心组件的初始化流程，我们就不能先理解Springboot的启动流程，这里不多做解释我直接给结果，如果想了解具体启动流程可以参看我之前的记录[《Spring-boot---SpringApplication运行阶段》](https://www.jianshu.com/p/6d4252d6bce4)虽然解释的不是很全面，但是大体上的流程已经解释的很清楚了。

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

我们来简单看一下源码

```java
/**
 * Build and validate a configuration model based on the registry of
 * {@link Configuration} classes.
 */
public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
   List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
    
    //no.1 获取注册表中已经存在的bean
   String[] candidateNames = registry.getBeanDefinitionNames();
    
   // no.2  选择出 启动类也就是标注 @SpringBootApplication 的类
    
   for (String beanName : candidateNames) {
      BeanDefinition beanDef = registry.getBeanDefinition(beanName);
      if (ConfigurationClassUtils.isFullConfigurationClass(beanDef) ||
            ConfigurationClassUtils.isLiteConfigurationClass(beanDef)) {
         if (logger.isDebugEnabled()) {
            logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
         }
      }
      else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
         configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
      }
   }

   // Return immediately if no @Configuration classes were found
   if (configCandidates.isEmpty()) {
      return;
   }

   // Sort by previously determined @Order value, if applicable
   configCandidates.sort((bd1, bd2) -> {
      int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
      int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
      return Integer.compare(i1, i2);
   });

   // Detect any custom bean name generation strategy supplied through the enclosing application context
   SingletonBeanRegistry sbr = null;
   if (registry instanceof SingletonBeanRegistry) {
      sbr = (SingletonBeanRegistry) registry;
      if (!this.localBeanNameGeneratorSet) {
         BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(CONFIGURATION_BEAN_NAME_GENERATOR);
         if (generator != null) {
            this.componentScanBeanNameGenerator = generator;
            this.importBeanNameGenerator = generator;
         }
      }
   }

   if (this.environment == null) {
      this.environment = new StandardEnvironment();
   }

   // Parse each @Configuration class   
    //no.3  生成解析注解的解析类对象
   ConfigurationClassParser parser = new ConfigurationClassParser(
         this.metadataReaderFactory, this.problemReporter, this.environment,
         this.resourceLoader, this.componentScanBeanNameGenerator, registry);

   Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
   Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
   do {
       //no.4  解析方法 后面会有介绍
      parser.parse(candidates);
       //no.5  校验是不是最终的配置类
      parser.validate();

      Set<ConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
      configClasses.removeAll(alreadyParsed);

      // Read the model and create bean definitions based on its content
      if (this.reader == null) {
         this.reader = new ConfigurationClassBeanDefinitionReader(
               registry, this.sourceExtractor, this.resourceLoader, this.environment,
               this.importBeanNameGenerator, parser.getImportRegistry());
      }
       //no.6 初始化所有的配置类
      this.reader.loadBeanDefinitions(configClasses);
      alreadyParsed.addAll(configClasses);

      candidates.clear();
       //no.7 将候选默认配置类和所有配置类融合
      if (registry.getBeanDefinitionCount() > candidateNames.length) {
         String[] newCandidateNames = registry.getBeanDefinitionNames();
         Set<String> oldCandidateNames = new HashSet<>(Arrays.asList(candidateNames));
         Set<String> alreadyParsedClasses = new HashSet<>();
         for (ConfigurationClass configurationClass : alreadyParsed) {
            alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
         }
         for (String candidateName : newCandidateNames) {
            if (!oldCandidateNames.contains(candidateName)) {
               BeanDefinition bd = registry.getBeanDefinition(candidateName);
               if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd, this.metadataReaderFactory) &&
                     !alreadyParsedClasses.contains(bd.getBeanClassName())) {
                  candidates.add(new BeanDefinitionHolder(bd, candidateName));
               }
            }
         }
         candidateNames = newCandidateNames;
      }
   }
   while (!candidates.isEmpty());

   // Register the ImportRegistry as a bean in order to support ImportAware @Configuration classes
   if (sbr != null && !sbr.containsSingleton(IMPORT_REGISTRY_BEAN_NAME)) {
      sbr.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser.getImportRegistry());
   }

   if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory) {
      // Clear cache in externally provided MetadataReaderFactory; this is a no-op
      // for a shared cache since it'll be cleared by the ApplicationContext.
      ((CachingMetadataReaderFactory) this.metadataReaderFactory).clearCache();
   }
}
```

在上面源码中，我已经把重要的方法用汉语标注在注释中，这里最需要注意的就是 no.4 ，解析方法 `parser.parse()`。在这个方法里 我们通过解析配置类注解中的模式注解，会层层查找到最终引用的类。我们来看一下这部分的源码。

```java
public void parse(Set<BeanDefinitionHolder> configCandidates) {
   this.deferredImportSelectors = new LinkedList<>();
 // 遍历选出的默认配置类集合 解析注解
   for (BeanDefinitionHolder holder : configCandidates) {
      BeanDefinition bd = holder.getBeanDefinition();
      try {
         if (bd instanceof AnnotatedBeanDefinition) {
            parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
         }
         else if (bd instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) bd).hasBeanClass()) {
            parse(((AbstractBeanDefinition) bd).getBeanClass(), holder.getBeanName());
         }
         else {
            parse(bd.getBeanClassName(), holder.getBeanName());
         }
      }
      catch (BeanDefinitionStoreException ex) {
         throw ex;
      }
      catch (Throwable ex) {
         throw new BeanDefinitionStoreException(
               "Failed to parse configuration class [" + bd.getBeanClassName() + "]", ex);
      }
   }

   //解析接口编程方式加载的模式注解
   processDeferredImportSelectors();
}
```

我们可以看到我们首先判断他们是否是接口编程的方式加载的，进而去解析他所引用的其他注解中的类（这里就是`@import`注解中的类 )。如果是接口编程方式的注解引用，则将这些注解类全部整理出来，通过调用`processDeferredImportSelectors` 把接口类全部筛选出来并排序。之后就调用源码中的 no.6 步骤 ，将所有配置类注册到注册表中。

最后在调用 `org.springframework.context.support.AbstractApplicationContext#registerBeanPostProcessors`  将注册表中的bean实例进行初始化并注册到上下文中 。

## 小结

​	在这里通篇简单的讲解了springboot初始化组件的配置组件的流程，虽然在debugger源码的时候花费了很多时间，但是确实是有所收获，讲解的不一定全都正确，大家可以按照这个思路来慢慢跟着来一遍，自己多做实践会比在网上看相关文章要理解深刻的多，并且网上的东西也不都是对的，需要我们自己去甄别。

