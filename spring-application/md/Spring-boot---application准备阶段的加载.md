# Spring-boot---application准备阶段的加载(一)

学习笔记是学习了 小马哥在慕课网的 《Spring Boot 2.0深度实践之核心技术篇》根据自己的需要和理解做的笔记。

### 应用上下文初始器(ApplicationContextInitializer)

利用Spring工厂机制,实现类`org.springframework.core.io.support.SpringFactoriesLoader` 

实例化 `ApplicationContextInitializer` 实现类，并排序对象集合。

自己追了一下源码,发现Spring-boot加载应用文上下初始器的顺序是

`org.springframework.boot.SpringApplication#SpringApplication()`->`org.springframework.core.io.support.SpringFactoriesLoader#loadFactoryNames`->`org.springframework.boot.SpringApplicatior#run()`

具体代码实现`org.springframework.boot.SpringApplication#SpringApplication()`

1.spring-boot使用`setInitializers`方法加载所有实现`ApplicationContextInitializer.class`的类，即全部上下文实现类。

```java
/**
 * Create a new {@link SpringApplication} instance. The application context will load
 * beans from the specified primary sources (see {@link SpringApplication class-level}
 * documentation for details. The instance can be customized before calling
 * {@link #run(String...)}.
 * @param resourceLoader the resource loader to use
 * @param primarySources the primary bean sources
 * @see #run(Class, String[])
 * @see #setSources(Set)
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
   this.resourceLoader = resourceLoader;
   Assert.notNull(primarySources, "PrimarySources must not be null");
   this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
   this.webApplicationType = deduceWebApplicationType();
   //加载上下文初始器 ApplicationContextInitializer.class 的实现类
   setInitializers((Collection) getSpringFactoriesInstances(
         ApplicationContextInitializer.class));
   //加载监听初始器 ApplicationListener.class 的实现类
   setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
   this.mainApplicationClass = deduceMainApplicationClass();
}
```

2.我们来看一下加载上下文实体的方法

`org.springframework.boot.SpringApplication#getSpringFactoriesInstances()`

```java
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type,
      Class<?>[] parameterTypes, Object... args) {
   ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
   // Use names and ensure unique to protect against duplicates
   //在这里调用了 SpringFactoriesLoader 中的loadFactoryNames方法，
    //来插入想要实现的上下文
   Set<String> names = new LinkedHashSet<>(
         SpringFactoriesLoader.loadFactoryNames(type, classLoader));
   List<T> instances = createSpringFactoriesInstances(type, parameterTypes,
         classLoader, args, names);
   AnnotationAwareOrderComparator.sort(instances);
   return instances;
}
```

 在这里注意一点就是spring-boot 使用了 `Thread.currentThread().getContextClassLoader()`来获取了当前类加载器，这里就要介绍一下SPI机制(我也是在看这段代码中,无法理解这段，在网上查了一些资料，简单的理解了一下这里为什么用这个方法。)

**SPI机制**（Service Provider Interface)其实源自服务提供者框架（Service Provider Framework，参考【EffectiveJava】page6)，是一种将服务接口与服务实现分离以达到解耦、大大提升了程序可扩展性的机制。引入服务提供者就是引入了spi接口的实现者，通过本地的注册发现获取到具体的实现类，轻松可插拔。

这里的`ClassLoader`是spring-boot的类加载器，在这突然又想到了JVM的**双亲委派模型**,感兴趣的同学可以了解一下。那么这个类加载器的SPI机制实现类都有哪些？ 就是配置资源`META-INF/spring.factories` 中的内容。

3.我们来看一下引用`SpringFactoriesLoader#SpringFactoriesLoader`的方法是做什么的。

```java
/**
 * Load the fully qualified class names of factory implementations of the
 * given type from {@value #FACTORIES_RESOURCE_LOCATION}, using the given
 * class loader.
 * @param factoryClass the interface or abstract class representing the factory
 * @param classLoader the ClassLoader to use for loading resources; can be
 * {@code null} to use the default
 * @see #loadFactories
 * @throws IllegalArgumentException if an error occurs while loading factory names
 */
public static List<String> loadFactoryNames(Class<?> factoryClass, @Nullable ClassLoader classLoader) {
   String factoryClassName = factoryClass.getName();
   return loadSpringFactories(classLoader).getOrDefault(factoryClassName, Collections.emptyList());
}
```

在源码注释中可以清晰的看到该方法的作用，首先要了解到`FACTORIES_RESOURCE_LOCATION`全局变量的定义

```java
/**
 * The location to look for factories.
 * <p>Can be present in multiple JAR files.
 */
public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";
```

META-INF/spring.factories存放的就是所有spring-boot实现的各种继承规约接口的实现类的类路径。

了解了这个全局变量，那么这段注释我们就可以明白了。

`/**Load the fully qualified class names of factory implementations of the given type from {@value #FACTORIES_RESOURCE_LOCATION}*/`从`FACTORIES_RESOURCE_LOCATION`中获取所有继承已知工厂类别的类全限定名。

4.既然都到这了，我们就不能不看一下`SpringFactoriesLoader#loadSpringFactories(classLoader)`这个方法。

```java
private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
   MultiValueMap<String, String> result = cache.get(classLoader);
   if (result != null) {
      return result;
   }

   try {
       /**
       * 获取所有实现工厂接口的类的全限定名
       */
      Enumeration<URL> urls = (classLoader != null ?
            classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
            ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
      result = new LinkedMultiValueMap<>();
      while (urls.hasMoreElements()) {
         URL url = urls.nextElement();
         UrlResource resource = new UrlResource(url);
         Properties properties = PropertiesLoaderUtils.loadProperties(resource);
         for (Map.Entry<?, ?> entry : properties.entrySet()) {
            List<String> factoryClassNames = Arrays.asList(
                  StringUtils.commaDelimitedListToStringArray((String) entry.getValue()));
            result.addAll((String) entry.getKey(), factoryClassNames);
         }
      }
      cache.put(classLoader, result);
      return result;
   }
   catch (IOException ex) {
      throw new IllegalArgumentException("Unable to load factories from location [" +
            FACTORIES_RESOURCE_LOCATION + "]", ex);
   }
}
```

通过 `classLoader.getResources`和`ClassLoader.getSystemResources`来获取`META-INF/spring.factories`下的内容。比如:

```properties
# Initializers
org.springframework.context.ApplicationContextInitializer=\
org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer,\
org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener
```

然后通过解析Properties内容来获取全限定名。

最后将想获取的全限定名返回给

`org.springframework.boot.SpringApplication#getSpringFactoriesInstances()`方法:

```java
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type,
      Class<?>[] parameterTypes, Object... args) {
   ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
   // Use names and ensure unique to protect against duplicates
   Set<String> names = new LinkedHashSet<>(
         SpringFactoriesLoader.loadFactoryNames(type, classLoader));
    //使用反射机制生成实例对象
   List<T> instances = createSpringFactoriesInstances(type, parameterTypes,
         classLoader, args, names);
    //进行实例集合排序
   AnnotationAwareOrderComparator.sort(instances);
   return instances;
}
```

通过`#createSpringFactoriesInstances` 方法生成实例以及调用`AnnotationAwareOrderComparator.sort（）`进行排序，而排序需要实现`org.springframework.core.Ordered`接口或者标注`org.springframework.core.Ordered`注解，排序不是强制性的。就像`META-INF/spring.factories`中应用上下文初始器的实现类 `org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer`和

`org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener`都没有实现排序的接口或者注解。



说了这么多，接下来让我们自己实现一个上下文初始器吧。



