# Springboot 2.0---WebFlux核心组件的初始化（二）

## 核心组件初始化流程

  既然说到初始化流程，那么就不能不上流程图了。

p12.png



   我们通过流程图相信已经可以理解百分四五十了,那么接下来让我们来通过源码来简单的解释一下详细流程。

   在之前的一篇笔记中，已经简单介绍了springboot加载配置组件流程的源码，那么现在我们就直接对照流程图的流程来对照下源码。真正的了解一下WebFlux核心组件的加载。



## 流程相关源码解析

   我们这里就直接查看需要加载的核心组件的流程，之前的注解配置解析流程不再重复，可以看之前的笔记。

我们从流程图上的组件 一个一个看

#### WebFluxAutoConfiguration派生其他类

```java
@Configuration
//条件装配 只有启动的类型是REACTIVE时加载
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
//只有存在 WebFluxConfigurer实例  时加载
@ConditionalOnClass(WebFluxConfigurer.class)
//在不存在  WebFluxConfigurationSupport实例时 加载
@ConditionalOnMissingBean({ WebFluxConfigurationSupport.class })
//在之后装配
@AutoConfigureAfter({ ReactiveWebServerFactoryAutoConfiguration.class,
      CodecsAutoConfiguration.class, ValidationAutoConfiguration.class })
//自动装配顺序
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
public class WebFluxAutoConfiguration {

    
   @Configuration
   @EnableConfigurationProperties({ ResourceProperties.class, WebFluxProperties.class })
   //接口编程 在装配WebFluxConfig 之前要先 装配EnableWebFluxConfiguration
   @Import({ EnableWebFluxConfiguration.class })
   public static class WebFluxConfig implements WebFluxConfigurer {
      //隐藏部分源码
       	/**
	 * Configuration equivalent to {@code @EnableWebFlux}.
	 */
   }
    
	@Configuration
	public static class EnableWebFluxConfiguration
			extends DelegatingWebFluxConfiguration {

		//隐藏部分代码
    }
    	@Configuration
	@ConditionalOnEnabledResourceChain
	static class ResourceChainCustomizerConfiguration {
        //隐藏部分代码
    }
    
    private static class ResourceChainResourceHandlerRegistrationCustomizer
			implements ResourceHandlerRegistrationCustomizer {
            //隐藏部分代码
        }
      
```

我们可以看到 由`WebFluxAutoConfiguration` 自动装配时确实由流程图中的所说 先自动装配 `EnableWebFluxConfiguration`  而`EnableWebFluxConfiguration`  继承了  `DelegatingWebFluxConfiguration` ，

`DelegatingWebFluxConfiguration` 继承了 `WebFluxConfigurationSupport`。

```java
@Configuration
public class DelegatingWebFluxConfiguration extends WebFluxConfigurationSupport {
...
}
```

所以我们来看一下 `WebFluxConfigurationSupport`   由于代码过长 我会把具体的实现代码省略掉

```java
public class WebFluxConfigurationSupport implements ApplicationContextAware {
   //跨域配置
   @Nullable
   private Map<String, CorsConfiguration> corsConfigurations;
   //路径HandlerMapping 对应的路径配置
   @Nullable
   private PathMatchConfigurer pathMatchConfigurer;
   //视图解析器
   @Nullable
   private ViewResolverRegistry viewResolverRegistry;
   //上下文
   @Nullable
   private ApplicationContext applicationContext;

   //声明DispatcherHandler 对象 注入到容器中
   @Bean
   public DispatcherHandler webHandler() {
      return new DispatcherHandler();
   }
   
   //异常处理器 
   @Bean
   @Order(0)
   public WebExceptionHandler responseStatusExceptionHandler() {
      return new WebFluxResponseStatusExceptionHandler();
   }

   //requestMappingHandlerMapping 和SrpingMvc组件一样
   @Bean
   public RequestMappingHandlerMapping requestMappingHandlerMapping() {
      RequestMappingHandlerMapping mapping = createRequestMappingHandlerMapping();
      mapping.setOrder(0);    //这里把映射顺序设置为 0 
      mapping.setContentTypeResolver(webFluxContentTypeResolver());
      mapping.setCorsConfigurations(getCorsConfigurations());    
      //省略部分代码
      return mapping;
   }
   //省略部分代码
   
   //Webflux内容协商处理器
   @Bean
   public RequestedContentTypeResolver webFluxContentTypeResolver() {
      RequestedContentTypeResolverBuilder builder = new RequestedContentTypeResolverBuilder();
      configureContentTypeResolver(builder);
      return builder.build();
   }
   //省略部分代码
  //WebFlux 的请求映射处理器 在 RequestMappingHandlerMapping 之前
   @Bean
   public RouterFunctionMapping routerFunctionMapping() {
      RouterFunctionMapping mapping = createRouterFunctionMapping();
      mapping.setOrder(-1); // go before RequestMappingHandlerMapping
      mapping.setMessageReaders(serverCodecConfigurer().getReaders());
      mapping.setCorsConfigurations(getCorsConfigurations());
      return mapping;
   }

  //省略部分代码
    //其他映射处理器 这里默认是 SimpleUrlHandlerMapping 
   @Bean
   public HandlerMapping resourceHandlerMapping() {
      ResourceLoader resourceLoader = this.applicationContext;
      if (resourceLoader == null) {
         resourceLoader = new DefaultResourceLoader();
      }
      ResourceHandlerRegistry registry = new ResourceHandlerRegistry(resourceLoader);
      addResourceHandlers(registry);
      AbstractHandlerMapping handlerMapping = registry.getHandlerMapping();
      if (handlerMapping != null) {
         PathMatchConfigurer configurer = getPathMatchConfigurer();
         Boolean useTrailingSlashMatch = configurer.isUseTrailingSlashMatch();
         Boolean useCaseSensitiveMatch = configurer.isUseCaseSensitiveMatch();
         if (useTrailingSlashMatch != null) {
            handlerMapping.setUseTrailingSlashMatch(useTrailingSlashMatch);
         }
         if (useCaseSensitiveMatch != null) {
            handlerMapping.setUseCaseSensitiveMatch(useCaseSensitiveMatch);
         }
      }
      else {
         handlerMapping = new EmptyHandlerMapping();
      }
      return handlerMapping;
   }
   //请求适配器 支持 WebFlux
   @Bean
   public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
      RequestMappingHandlerAdapter adapter = createRequestMappingHandlerAdapter();
      adapter.setMessageReaders(serverCodecConfigurer().getReaders());
      adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer());
      adapter.setReactiveAdapterRegistry(webFluxAdapterRegistry());
      ArgumentResolverConfigurer configurer = new ArgumentResolverConfigurer();
      configureArgumentResolvers(configurer);
      adapter.setArgumentResolverConfigurer(configurer);

      return adapter;
   }
   //省略部分代码
   //国际化解析器
   @Bean
   public LocaleContextResolver localeContextResolver() {
      return createLocaleContextResolver();
   }

   //省略部分代码

   //WebFlux路由形式的适配器
   @Bean
   public HandlerFunctionAdapter handlerFunctionAdapter() {
      return new HandlerFunctionAdapter();
   }
   //省略部分代码

}
```

我们可以看到 `WebFluxConfigurationSupport` 不仅配置`DispatcherHandler` 还同时配置了其他很多WebFlux核心组件包括异常处理,内容协商处理器等。

### ApplicationContext依赖查找 WebHandler等操作

组件都有了 那么我们时何时注入到上容器下文中的呢？ 在这里 其实是在 刷新上下文时注入的，核心代码如下

org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext#onRefresh

```java
@Override
protected void onRefresh() {
   super.onRefresh();
   try {
       //创建Reactive Web Server
      createWebServer();
   }
   catch (Throwable ex) {
      throw new ApplicationContextException("Unable to start reactive web server",
            ex);
   }
}
```

我们可以看到在刷新上下文的时候，我们调用了 `createWebServer()` 方法来创建 Reactive Web Server。

```java
private void createWebServer() {
   WebServer localServer = this.webServer;
   if (localServer == null) {
      this.webServer = getWebServerFactory().getWebServer(getHttpHandler());
   }
   initPropertySources();
}
```

在这段代码就是把之前的 `DispatcherHandler`  与上下文联系了起来,而通过`getHttpHandler() ` 调用将 `HttpHandlerAutoConfiguration`实例化。

```java
@Configuration
@ConditionalOnClass({ DispatcherHandler.class, HttpHandler.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnMissingBean(HttpHandler.class)
@AutoConfigureAfter({ WebFluxAutoConfiguration.class })
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
public class HttpHandlerAutoConfiguration {

   @Configuration
   public static class AnnotationConfig {

      private ApplicationContext applicationContext;

      public AnnotationConfig(ApplicationContext applicationContext) {
         this.applicationContext = applicationContext;
      }

      //构建WebHandler
      @Bean
      public HttpHandler httpHandler() {
         return WebHttpHandlerBuilder.applicationContext(this.applicationContext)
               .build();
      }

   }

}
```

直接通过 `org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration.AnnotationConfig#httpHandler` 构建WebHadler 。构建的内容就如开头的流程图一样 ，下面配上源码 ，相信大家一看就懂了。

```java
/**
 * Build the {@link HttpHandler}.
 */
public HttpHandler build() {
    //声明 FilteringWebHandler
   WebHandler decorated = new FilteringWebHandler(this.webHandler, this.filters);
    //包装 ExceptionHandlingWebHandler
   decorated = new ExceptionHandlingWebHandler(decorated,  this.exceptionHandlers);

    //继续包装
   HttpWebHandlerAdapter adapted = new HttpWebHandlerAdapter(decorated);
   
    //配置注入
   if (this.sessionManager != null) {
      adapted.setSessionManager(this.sessionManager);
   }
   if (this.codecConfigurer != null) {
      adapted.setCodecConfigurer(this.codecConfigurer);
   }
   if (this.localeContextResolver != null) {
      adapted.setLocaleContextResolver(this.localeContextResolver);
   }
   if (this.applicationContext != null) {
      adapted.setApplicationContext(this.applicationContext);
   }

   return adapted;
}
```

主要的装配流程已经简单的介绍完毕。



## 总结

  IT行业技术更新是在太快，以上的内容都是为了方便以后自己回头看的时候可以很快的回忆起当时学习的内容，说实话，从这段debugger源码的过程中，收获很多，让我更进一步了解了SpringBoot的自动装配。虽然现在的理解可能是冰山一角，但是随着我们慢慢的深入或者大牛的分享，相信会更深一步的理解博大精深的Spring。



