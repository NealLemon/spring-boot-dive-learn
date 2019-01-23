# Springboot--扩展外部化配置(三)

  接着之前Springboot--扩展外部化配置(二) 来继续实现外部化的扩展。我们将剩余的几个扩展给介绍完毕。

- 基于 `ApplicationContextInitializer` 扩展外部化配置属性源
- 基于 `SpringApplicationRunListener#contextPrepared` 扩展外部化配置属性源
- 基于 `SpringApplicationRunListener#contextLoaded` 扩展外部化配置属性源



### 前置内容

  在Springboot--扩展外部化配置(一) 中我们有了解到在SpringBoot启动过程中在创建完 `Environment`之后，就会准备上下文`org.springframework.boot.SpringApplication#prepareContext` 。我们接下来的三种外部化配置实现方式 都是基于这个方法的执行顺序实现的。可以详细看一下贴出的源码中的注释。

```java
private void prepareContext(ConfigurableApplicationContext context,
      ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
      ApplicationArguments applicationArguments, Banner printedBanner) {
   context.setEnvironment(environment);
   postProcessApplicationContext(context);
    //执行ApplicationContextInitializer SPI接口 initialize
    //用于 基于 ApplicationContextInitializer 扩展外部化配置属性源
   applyInitializers(context);
    
   //SpringApplicationRunListener 实现类遍历监听contextPrepared
   //用于 基于 SpringApplicationRunListener#contextPrepared 扩展外部化配置属性源
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
   load(context, sources.toArray(new Object[0]));
   //SpringApplicationRunListener 实现类遍历监听contextLoaded
    //用于 基于 SpringApplicationRunListener#contextLoaded 扩展外部化配置属性源
   listeners.contextLoaded(context);
}
```

  通过我标注注释的地方，大家可能也就了解到了之后我们准备怎么去实现剩下的外部化配置方式。其实无非就是通过SpringBoot在初始化上下文之前，调用可以调用的对外接口，进行配置。

  这里我们主要是注意因为我们在扩展外部化配置时 使用的是 `org.springframework.core.env.MutablePropertySources#addFirst` 方法，默认会把配置的外部化资源放在第一位。所以我们要注意`prepareContext()` 方法中的这些过程的执行顺序，换局话说，执行越晚，那么其配置的资源则会放在最前面。



### 基于 `ApplicationContextInitializer` 扩展外部化配置属性源

#### 实现基础

  现在我们就使用在 `org.springframework.boot.SpringApplication#prepareContext`中的 `applyInitializers(context);` 方法(目的是对应用程序上下文进行初始化)，做切入点，让我们也简单实现一个自定义外部资源的初始化操作。

#### 具体实现

1.实现`ApplicationContextInitializer` `接口类 ExternalizeApplicationContextInitializer.java

```java
/**
 * @ClassName ExternalizeApplicationContextInitializer
 * @Description 自定义上下文初始化实现类
 * @Author Neal
 * @Date 2019/1/23 19:05
 * @Version 1.0
 */
public class ExternalizeApplicationContextInitializer implements ApplicationContextInitializer {


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        //获取environment 对象
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        //获取项目跟路径
        String classpath = ExternalizeApplicationContextInitializer.class.getResource("/").getPath();
        //获取PropertySource组合对象
        MutablePropertySources propertySources = environment.getPropertySources();
        //获取自定义的外部化配置资源
        File file = new File(classpath +"config/applicationinitializer.properties");

        /**
         * 获取Property对象
         */
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            //声明一个properties对象
            Properties properties = new Properties();
            // 加载字符流成为 Properties 对象
            properties.load(reader);
            //声明Spring内置PropertiesPropertySource对象
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---ExternalizeApplicationContextInitializer",properties);
            //将配置资源放到其他配置资源的首位
            propertySources.addFirst(propertySource);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
```

2.配置自定义的外部化资源，`resources\config\applicationinitializer.properties`

```properties
#ExternalizeApplicationContextInitializer 对应的配置
test=5
```

3.将自定义的`ApplicationContextInitializer` 加入到 `resources\META-INF\spring.factories`

```properties
# Run Listeners
org.springframework.boot.SpringApplicationRunListener=\
neal.externalized.listener.ExternalizePropertyListener
#ApplicationListener
org.springframework.context.ApplicationListener=\
neal.externalized.listener.ExternalizePropertyEventListener

#EnvironmentPostProcessor
org.springframework.boot.env.EnvironmentPostProcessor=\
neal.externalized.listener.ExternalizePropertyPostProcessor

#ApplicationContextInitializer
org.springframework.context.ApplicationContextInitializer=\
neal.externalized.listener.ExternalizeApplicationContextInitializer
```



#### 执行结果

我们启动容器，查看控制台输出

p8.png



这里我们注意到，我们的测试的值已经变成了所配置的5，但是还有一点 `ConfigurationPropertySourcesPropertySource` 这个类的执行是在 我们自定义`ExternalizeApplicationContextInitializer`之前的，所以我们刚刚自定义外部化配置才会放在首位执行。



### 基于 `SpringApplicationRunListener#contextPrepared` 扩展外部化配置属性源

#### 实现基础

  使用在 `org.springframework.boot.SpringApplication#prepareContext`中的 `   listeners.contextPrepared(context);` 方法(上下文准备完成)，做切入点，简单实现一个自定义外部资源的初始化操作。

  这段的实现其实在我们之前已经自定义了`SpringApplicationRunListener` 事件监听，就是我们介绍的第一种扩展方式(基于 `SpringApplicationRunListener#environmentPrepared`的实现方式)中使用的`ExternalizePropertyListener`。

#### 具体实现

1.重写`ExternalizePropertyListener#contextPrepared()` 方法。这里用到了读取Properties文件的方法，由于是DEMO，我就不单独抽出来重构了。

```java
/**
 * 扩展 {@link PropertySource}
 */
public class ExternalizePropertyListener implements SpringApplicationRunListener,Ordered {

    private final SpringApplication application;

    private final String[] args;

    public ExternalizePropertyListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void starting() {

    }

    /**
     * 基于 `SpringApplicationRunListener#environmentPrepared`的实现方式
     * @param environment
     */
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {

        //获取项目跟路径
        String classpath = ExternalizePropertyListener.class.getResource("/").getPath();
        //获取PropertySource组合对象
        MutablePropertySources propertySources = environment.getPropertySources();

        //获取自定义的外部化配置资源
        File file = new File(classpath +"config/externalizepropertylistener.properties");
        /**
         * 获取Property对象
         */
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            //声明一个properties对象
            Properties properties = new Properties();
            // 加载字符流成为 Properties 对象
            properties.load(reader);
            //声明Spring内置PropertiesPropertySource对象
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---ExternalizePropertyListener",properties);
            //将配置资源放到其他配置资源的首位
            propertySources.addFirst(propertySource);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     *     基于 `SpringApplicationRunListener#contextPrepared` 扩展外部化配置属性源
     */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        //获取environment 对象
        ConfigurableEnvironment environment = context.getEnvironment();

        //获取项目跟路径
        String classpath = ExternalizePropertyListener.class.getResource("/").getPath();
        //获取PropertySource组合对象
        MutablePropertySources propertySources = environment.getPropertySources();
        //获取自定义的外部化配置资源
        File file = new File(classpath +"config/contextprepared.properties");

        /**
         * 获取Property对象
         */
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            //声明一个properties对象
            Properties properties = new Properties();
            // 加载字符流成为 Properties 对象
            properties.load(reader);
            //声明Spring内置PropertiesPropertySource对象
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---contextPrepared",properties);
            //将配置资源放到其他配置资源的首位
            propertySources.addFirst(propertySource);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void started(ConfigurableApplicationContext context) {

    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }
    //加载顺序在EventPublishingRunListener之前
    // 这么做是为了之后的外部化配置展示
    @Override
    public int getOrder() {
        return -1;
    }
}
```

这里我贴出了全部方法，在这一部分，我们只需要关心`#contextPrepared()`的实现。

2.配置自定义的外部化资源，`resources\config\contextprepared.properties`

```properties
#SpringApplicationRunListener#contextPrepared 对应的配置
test=6
```

 3.由于之前在`resources\META-INF\spring.factories`添加过监听，所以这里不需要做任何操作



#### 执行结果

p9.png

执行结果，不出意外，确实是我们最新配置的值。



### 基于 `SpringApplicationRunListener#contextLoaded` 扩展外部化配置属性源

#### 实现基础

  我们接着使用在 `org.springframework.boot.SpringApplication#prepareContext`中的 `listeners.contextLoaded(context);` 方法(加载应用上下文)，做切入点，简单实现一个自定义外部资源的初始化操作。

  同样，这段的实现跟上面的实现一样，都是在之前介绍的第一种扩展方式(基于 `SpringApplicationRunListener#environmentPrepared`的实现方式)中使用的`ExternalizePropertyListener`。

#### 具体实现

1.重写`ExternalizePropertyListener#contextLoaded（） `方法。这里一样我们直接copy之前的实现，不做重构。

```java
/**
 * 扩展 {@link PropertySource}
 */
public class ExternalizePropertyListener implements SpringApplicationRunListener,Ordered {

    private final SpringApplication application;

    private final String[] args;

    public ExternalizePropertyListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void starting() {

    }

    /**
     * 基于 `SpringApplicationRunListener#environmentPrepared`的实现方式
     * @param environment
     */
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {

        //获取项目跟路径
        String classpath = ExternalizePropertyListener.class.getResource("/").getPath();
        //获取PropertySource组合对象
        MutablePropertySources propertySources = environment.getPropertySources();

        //获取自定义的外部化配置资源
        File file = new File(classpath +"config/externalizepropertylistener.properties");
        /**
         * 获取Property对象
         */
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            //声明一个properties对象
            Properties properties = new Properties();
            // 加载字符流成为 Properties 对象
            properties.load(reader);
            //声明Spring内置PropertiesPropertySource对象
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---ExternalizePropertyListener",properties);
            //将配置资源放到其他配置资源的首位
            propertySources.addFirst(propertySource);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     *     基于 `SpringApplicationRunListener#contextPrepared` 扩展外部化配置属性源
     */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        //获取environment 对象
        ConfigurableEnvironment environment = context.getEnvironment();

        //获取项目跟路径
        String classpath = ExternalizePropertyListener.class.getResource("/").getPath();
        //获取PropertySource组合对象
        MutablePropertySources propertySources = environment.getPropertySources();
        //获取自定义的外部化配置资源
        File file = new File(classpath +"config/contextprepared.properties");

        /**
         * 获取Property对象
         */
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            //声明一个properties对象
            Properties properties = new Properties();
            // 加载字符流成为 Properties 对象
            properties.load(reader);
            //声明Spring内置PropertiesPropertySource对象
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---contextPrepared",properties);
            //将配置资源放到其他配置资源的首位
            propertySources.addFirst(propertySource);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 基于 `SpringApplicationRunListener#contextLoaded` 扩展外部化配置属性源
     * @param context
     */
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        //获取environment 对象
        ConfigurableEnvironment environment = context.getEnvironment();

        //获取项目跟路径
        String classpath = ExternalizePropertyListener.class.getResource("/").getPath();
        //获取PropertySource组合对象
        MutablePropertySources propertySources = environment.getPropertySources();
        //获取自定义的外部化配置资源
        File file = new File(classpath +"config/contextploaded.properties");

        /**
         * 获取Property对象
         */
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            //声明一个properties对象
            Properties properties = new Properties();
            // 加载字符流成为 Properties 对象
            properties.load(reader);
            //声明Spring内置PropertiesPropertySource对象
            PropertiesPropertySource propertySource = new PropertiesPropertySource("from---contextLoaded",properties);
            //将配置资源放到其他配置资源的首位
            propertySources.addFirst(propertySource);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void started(ConfigurableApplicationContext context) {

    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }
    //加载顺序在EventPublishingRunListener之前
    // 这么做是为了之后的外部化配置展示
    @Override
    public int getOrder() {
        return -1;
    }
}
```

这里也贴出了全部方法，在这一部分，我们只需要关心`#contextLoaded()`的实现。

2.配置自定义的外部化资源，`resources\config\contextloaded.properties`

```properties
#ExternalizePropertyListener.contextLoaded 对应的配置
test=7
```

 3.同样,之前在`resources\META-INF\spring.factories`添加过监听，所以这里不需要做任何操作。



#### 执行结果

p10.png

 不出意外，我们也同样改变了扩展的值。





### 小结

  对于外部化配置的方面，相信在之后的开发中会有很大的用途，特别是各种整合。希望在接下来的实际开发中，我们可以用到这些实现。