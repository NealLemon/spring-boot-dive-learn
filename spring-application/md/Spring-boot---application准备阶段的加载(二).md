# Spring-boot---application准备阶段的加载(二)

学习笔记是学习了 小马哥在慕课网的 《Spring Boot 2.0深度实践之核心技术篇》根据自己的需要和理解做的笔记。

### 自定义应用上下文初始器(ApplicationContextInitializer)

在上一篇中介绍了 `ApplicationContextInitializer`的基本实现机制。那么就让我们来试试自己创建一个应用上下文，来实践一下。

1.首先在自己的项目中的本地资源`resources`目录下创建`META-INF/spring.factories` 。我本地的目录如图所示

		目录.png

2.如果我们不知道如何实现一个自定义上下文实现类，我们可以参考一下spring-boot中的`org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer` 实现类。

```java
/**
 * {@link ApplicationContextInitializer} to create a shared
 * {@link CachingMetadataReaderFactory} between the
 * {@link ConfigurationClassPostProcessor} and Spring Boot.
 *
 * @author Phillip Webb
 * @since 1.4.0
 */
class SharedMetadataReaderFactoryContextInitializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

   public static final String BEAN_NAME = "org.springframework.boot.autoconfigure."
         + "internalCachingMetadataReaderFactory";

   @Override
   public void initialize(ConfigurableApplicationContext applicationContext) {
      applicationContext.addBeanFactoryPostProcessor(
            new CachingMetadataReaderFactoryPostProcessor());
   }
```

我在这里只截取了一部分,对比`ApplicationContextInitializer` 接口代码,就可以模仿实现自己的实现类了。

```java
public interface ApplicationContextInitializer<C extends ConfigurableApplicationContext> {

   /**
    * Initialize the given application context.
    * @param applicationContext the application to configure
    */
   void initialize(C applicationContext);

}
```

我们可以看到只需要简单实现 `initialize（）`方法。

3.现在让我们在项目中创建我们自定义的应用上下文实现类。这里要创建两个类 实现不同的排序接口。

- `HelloWorldApplicationContextInitializer`  注解了`@Order` 注解接口,很简单的实现。我们把加载后的排序设置为最高优先级。具体代码如下:

```java
@Order(Ordered.HIGHEST_PRECEDENCE) 
public class HelloWorldApplicationContextInitializer<C extends ConfigurableApplicationContext> implements ApplicationContextInitializer<C> {
    @Override
    public void initialize(C applicationContext) {
        System.out.println("HelloWorldApplicationContextInitializer:id=" + applicationContext.getId());
    }
}
```

- `AfterHelloWorldApplicationContextInitializer` 实现了`Ordered`接口,我们把加载后的排序设置为最低优先级。具体代码如下:

 ```java
public class AfterHelloWorldApplicationContextInitializer<C extends ConfigurableApplicationContext> implements ApplicationContextInitializer<C>,Ordered {
      @Override
      public void initialize(C applicationContext) {
          System.out.println("AfterHelloWorldApplicationContextInitializer:id=" + applicationContext.getId());
      }
  
      @Override
      public int getOrder() {
          return Ordered.LOWEST_PRECEDENCE;
      }
  }
 ```

4.简单的自定义实现类已经都搞完了，下面启动spring-boot，按照理想状态是 `HelloWorldApplicationContextInitializer`  先于 `AfterHelloWorldApplicationContextInitializer`  加载。现在让我们启动一下，看看结果。

结果.png



#### 总结

  虽然目前很少用到自定义上下文初始器，但是当我们要设置上下文`Environment`时或者获取`ConfigurableListableBeanFactory`实现类时，就可以有用武之地了。相关解释：

- Environment:是对配置文件(*profiles* )和属性文件(*properties*)两个关键应用环境方面的建模。提供激活和默认的配置文件和	操作底层配置资源的功能。
- ConfigurableListableBeanFactory：提供解析和修改 bean 定义以及准备实例化单例bean 的功能。

