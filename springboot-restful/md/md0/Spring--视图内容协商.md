# Spring--视图内容协商(一)

本文是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。 

所谓的视图内容协商，就是让Web客户端根据不同的请求策略，实现服务端响应对应视图的内容输出。 接下来让我们深入的了解一下到底Spring是如何视图内容协商的。

## 核心组件

- 视图解析器:`ContentNegotiatingViewResolver`
- 内容协商管理器:`ContentNegotiationManager`
- 内容协商策略:`ContentNegotiationStrategy`

## 源码解读前置工作

在我们要理解Spring的视图内容协调流程图之前，我们需要新建一个spring-boot项目，然后进行必要的配置来启动视图内容协商。我们新建一个模块名为 `springboot-restful` 之所以起这个名字,是因为视图内容协商不仅是对客户端视图渲染的协商操作，也是针对`restful`形式的内容的请求和响应的协商操作。

1.新建Model--    **springboot-restful**

目录.png

2.pom.xml

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
    </dependency>

    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>jstl</artifactId>
    </dependency>

    <!-- Provided -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.tomcat.embed</groupId>
        <artifactId>tomcat-embed-jasper</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

3.其他代码

```java
/**
 * Spring 拦截器 配置
 */
@Configuration   //配置
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置视图内容协商
     * @param configurer
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorParameter(true).favorPathExtension(true);

    }


    /**
     * 解决在IDEA下maven多模块使用spring-boot跳转JSP 404问题
     * @return
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
        return (factory -> {
            factory.addContextCustomizers(context -> {
                //当前webapp路径
                String relativePath = "springboot-restful/src/main/webapp";
                File docBaseFile = new File(relativePath);
                if(docBaseFile.exists()) {
                    context.setDocBase(new File(relativePath).getAbsolutePath());
                }
            });
        });
    }

}
```



```java
/**
 * Spring-boot 启动引导类
 */
@SpringBootApplication
public class SpringBootRestfulBootStrap {


    public static void main(String[] args) {
        SpringApplication.run(SpringBootRestfulBootStrap.class,args);

    }
}
```





## 流程图

流程图.png

## 源码解读

我们可以根据上面的流程图来一起阅读源码，这样能让我们有个初步的理解。

**步骤一**

首先我们来看一下`Spring-boot`是什么时候开始初始化声明`ContentNegotiationConfigurer`。

`org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.EnableWebMvcConfiguration#mvcContentNegotiationManager`

在这个方法中,我们可以看到在容器初始化时，`Spring-boot`的自动装配就把`ContentNegotiationManager`装在到容器了。

```java
@Bean
@Override
public ContentNegotiationManager mvcContentNegotiationManager() {
   ContentNegotiationManager manager = super.mvcContentNegotiationManager();
   List<ContentNegotiationStrategy> strategies = manager.getStrategies();
   ListIterator<ContentNegotiationStrategy> iterator = strategies.listIterator();
   while (iterator.hasNext()) {
      ContentNegotiationStrategy strategy = iterator.next();
      if (strategy instanceof PathExtensionContentNegotiationStrategy) {
         iterator.set(new OptionalPathExtensionContentNegotiationStrategy(
               strategy));
      }
   }
   return manager;
}
```

`super.mvcContentNegotiationManager()`源码:

```java
/**
 * Return a {@link ContentNegotiationManager} instance to use to determine
 * requested {@linkplain MediaType media types} in a given request.
 */
@Bean
public ContentNegotiationManager mvcContentNegotiationManager() {
   if (this.contentNegotiationManager == null) {
      ContentNegotiationConfigurer configurer = new ContentNegotiationConfigurer(this.servletContext);
      configurer.mediaTypes(getDefaultMediaTypes());
      configureContentNegotiation(configurer);
      this.contentNegotiationManager = configurer.buildContentNegotiationManager();
   }
   return this.contentNegotiationManager;
}
```

在这里通过`ContentNegotiationConfigurer`来创建`ContentNegotiationManager` 对象，我们先来看一下`ContentNegotiationConfigurer`中都有哪些关键的方法。

```java
public class ContentNegotiationConfigurer {

   private final ContentNegotiationManagerFactoryBean factory = new ContentNegotiationManagerFactoryBean();
    .......
        	/**
	 * Build a {@link ContentNegotiationManager} based on this configurer's settings.
	 * @since 4.3.12
	 * @see ContentNegotiationManagerFactoryBean#getObject()
	 */
	protected ContentNegotiationManager buildContentNegotiationManager() {
		this.factory.addMediaTypes(this.mediaTypes);
		return this.factory.build();
	}

}
```

在这里 我们可以看到`ContentNegotiationConfigurer` 类中，声明了一个`ContentNegotiationManagerFactoryBean`这也如流程图中的 步骤1 ---- 关联。

**步骤二**

 配置策略  则是使用`ContentNegotiationConfigurer` 的几个方法来配置，这里我们在自定义的`com.web.configuration.WebMvcConfig`中只使用了两种配置。代码如下

```java
@Override
public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.favorParameter(true).favorPathExtension(true);

}
```

对应的方法代码如下:

```java
	/**
	 * Whether a request parameter ("format" by default) should be used to
	 * determine the requested media type. For this option to work you must
	 * register {@link #mediaType(String, MediaType) media type mappings}.
	 * <p>By default this is set to {@code false}.
	 * @see #parameterName(String)
	 */
//请求参数
public ContentNegotiationConfigurer favorParameter(boolean favorParameter) {
   this.factory.setFavorParameter(favorParameter);
   return this;
}
	/**
	 * Whether the path extension in the URL path should be used to determine
	 * the requested media type.
	 * <p>By default this is set to {@code true} in which case a request
	 * for {@code /hotels.pdf} will be interpreted as a request for
	 * {@code "application/pdf"} regardless of the 'Accept' header.
	 */
//URL后缀
public ContentNegotiationConfigurer favorPathExtension(boolean favorPathExtension) {
	this.factory.setFavorPathExtension(favorPathExtension);
	return this;
}
```

这里就不多做解释了，感兴趣的小伙伴可以看一下英文注释，也就轻轻松松明白了。

**步骤三/步骤四**

添加策略和创建ContentNegotiationManager 放在一起讲。

我们可以从步骤一的源码中看到 最后是调用`ContentNegotiationConfigurer`中的`buildContentNegotiationManager()`方法来创建`ContentNegotiationManager `的。

那么我们来进一步看一下代码。

`org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer#buildContentNegotiationManager`

```java
protected ContentNegotiationManager buildContentNegotiationManager() {
   this.factory.addMediaTypes(this.mediaTypes);
   return this.factory.build();
}
```



这个方法在步骤一中我就有贴出，因为非常重要，在这我们可以看到，其实是使用`ContentNegotiationManagerFactoryBean`来创建`ContentNegotiationManager `的。我们来看一下`build()`方法。

```java
/**
 * Actually build the {@link ContentNegotiationManager}.
 * @since 5.0
 */
public ContentNegotiationManager build() {
   List<ContentNegotiationStrategy> strategies = new ArrayList<>();

   if (this.strategies != null) {
      strategies.addAll(this.strategies);
   }
   else {
       //是否配置 URL后缀策略
      if (this.favorPathExtension) {
          //声明并配置 PathExtensionContentNegotiationStrategy 策略
         PathExtensionContentNegotiationStrategy strategy;
         if (this.servletContext != null && !useRegisteredExtensionsOnly()) {
            strategy = new ServletPathExtensionContentNegotiationStrategy(this.servletContext, this.mediaTypes);
         }
         else {
            strategy = new PathExtensionContentNegotiationStrategy(this.mediaTypes);
         }
         strategy.setIgnoreUnknownExtensions(this.ignoreUnknownPathExtensions);
         if (this.useRegisteredExtensionsOnly != null) {
            strategy.setUseRegisteredExtensionsOnly(this.useRegisteredExtensionsOnly);
         }
         strategies.add(strategy);
      }

       //是否配置了参数策略
      if (this.favorParameter) {
          //声明并配置ParameterContentNegotiationStrategy策略
         ParameterContentNegotiationStrategy strategy = new ParameterContentNegotiationStrategy(this.mediaTypes);
         strategy.setParameterName(this.parameterName);
         if (this.useRegisteredExtensionsOnly != null) {
            strategy.setUseRegisteredExtensionsOnly(this.useRegisteredExtensionsOnly);
         }
         else {
            strategy.setUseRegisteredExtensionsOnly(true);  // backwards compatibility
         }
         strategies.add(strategy);
      }

      if (!this.ignoreAcceptHeader) {
         strategies.add(new HeaderContentNegotiationStrategy());
      }

      if (this.defaultNegotiationStrategy != null) {
         strategies.add(this.defaultNegotiationStrategy);
      }
   }

   this.contentNegotiationManager = new ContentNegotiationManager(strategies);
   return this.contentNegotiationManager;
}
```

一下贴出这么多代码可能有些懵，但是我们对照着流程图一步一步的看，由于我们已经配置了 <u>参数策略</u>以及<u>URL后缀策略</u>。所以 上面的if else 就很好懂了。看注释就可以明白了。



**步骤五**

关联`ContentNegotiatingViewResolver`通过`org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#viewResolver`方法，来初始化Bean `ContentNegotiatingViewResolver` 。可以看到方法第二行就是关联`ContentNegotiationManager`的地方。

```java
@Bean
@ConditionalOnBean(ViewResolver.class)
@ConditionalOnMissingBean(name = "viewResolver", value = ContentNegotiatingViewResolver.class)
public ContentNegotiatingViewResolver viewResolver(BeanFactory beanFactory) {
   ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
    //关联 ContentNegotiationManager
   resolver.setContentNegotiationManager(
         beanFactory.getBean(ContentNegotiationManager.class));
   // ContentNegotiatingViewResolver uses all the other view resolvers to locate
   // a view so it should have a high precedence
   resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
   return resolver;
}
```



**步骤六**

`ViewResolver` Bean 关联

这一步骤非常的绕，看了小马哥的视频后，debugger了很久，不明白是在何时`ContentNegotiatingViewResolver`调用方法关联其他`ViewResolver`的。

`org.springframework.web.servlet.view.ContentNegotiatingViewResolver#initServletContext`

我们先来看源码:

```java
@Override
protected void initServletContext(ServletContext servletContext) {
    //获取到所有 ViewResolvers
   Collection<ViewResolver> matchingBeans =
         BeanFactoryUtils.beansOfTypeIncludingAncestors(obtainApplicationContext(), ViewResolver.class).values();
    //关联他们 
   if (this.viewResolvers == null) {
      this.viewResolvers = new ArrayList<>(matchingBeans.size());
      for (ViewResolver viewResolver : matchingBeans) {
         if (this != viewResolver) {
            this.viewResolvers.add(viewResolver);
         }
      }
   }
   else {
      for (int i = 0; i < this.viewResolvers.size(); i++) {
         ViewResolver vr = this.viewResolvers.get(i);
         if (matchingBeans.contains(vr)) {
            continue;
         }
         String name = vr.getClass().getName() + i;
         obtainApplicationContext().getAutowireCapableBeanFactory().initializeBean(vr, name);
      }

   }
   if (this.viewResolvers.isEmpty()) {
      logger.warn("Did not find any ViewResolvers to delegate to; please configure them using the " +
            "'viewResolvers' property on the ContentNegotiatingViewResolver");
   }
    //排序
   AnnotationAwareOrderComparator.sort(this.viewResolvers);
   this.cnmFactoryBean.setServletContext(servletContext);
}
```

从代码中我们可以很清晰的看到 首先 先获取到所有的`ViewResolver` 然后遍历关联。

但是，到底是什么时候关联的呢。

经过debugger是在 

`org.springframework.boot.SpringApplication#run(java.lang.String...)`启动方法中的 `refreshContext(context);`这个调用后就可以关联上了。那么为什么会这样，我们可以看到

```java
public class ContentNegotiatingViewResolver extends WebApplicationObjectSupport
      implements ViewResolver, Ordered, InitializingBean {
      ...
      ｝
    
```

`ContentNegotiatingViewResolver`继承了 `WebApplicationObjectSupport` 。

这个类中有这么一个方法

`org.springframework.web.context.support.WebApplicationObjectSupport#initApplicationContext`

```java
@Override
protected void initApplicationContext(ApplicationContext context) {
   super.initApplicationContext(context);
   if (this.servletContext == null && context instanceof WebApplicationContext) {
      this.servletContext = ((WebApplicationContext) context).getServletContext();
      if (this.servletContext != null) {
         initServletContext(this.servletContext);
      }
   }
}
```

那么是在什么时候触发的这个方法呢。

由于过程是在太复杂，只把最后几步贴出来。

1.`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#initializeBean`
2.`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsBeforeInitialization`
3.`org.springframework.context.support.ApplicationContextAwareProcessor#postProcessBeforeInitialization`
4.`org.springframework.context.support.ApplicationContextAwareProcessor#invokeAwareInterfaces`

最后在`invokeAwareInterfaces`这个方法里调用`setApplicationContext`

```java
if (bean instanceof ApplicationContextAware) {
   ((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
}
```



其实这么复杂的过程，用一句简单的话总结就是`Spring-boot`的一个关于在访问 `ServletContext`的一个回调接口，来自定义初始化。



## 总结

 以上的内容其实有点难懂，感兴趣的小伙伴可以仔细研究一下，毕竟理解`Spring`的功能设计比使用API接口要难得多。共勉加油。