# 简单理解SpringMvc模块组件

本文是基于慕课网小马哥的 《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。

之前的文章[理解SpringMvc架构以及流程](https://www.jianshu.com/p/6433a07f909c)使用非资源配置(不使用XML文件)搭建一个Spring项目。并没有说明SpringMVC是如何加载各个组件的。接下来让我们理解一下这部分内容。

## @EnableWebMvc 注解

之前介绍过Spring的模式注解，其实@EnableWebMvc就是一个模式注解，我们来看一下这个注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {
}
```

我们可以看到这个注解使用了驱动注解编程,通过引用`DelegatingWebMvcConfiguration`这个类来实现装配，但是我们进一步看一下`DelegatingWebMvcConfiguration`这个类，发现这个类中没有任何一个Bean定义。

```java
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {

   private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();


   @Autowired(required = false)
   public void setConfigurers(List<WebMvcConfigurer> configurers) {
      if (!CollectionUtils.isEmpty(configurers)) {
         this.configurers.addWebMvcConfigurers(configurers);
      }
   }


   @Override
   protected void configurePathMatch(PathMatchConfigurer configurer) {
      this.configurers.configurePathMatch(configurer);
   }

   @Override
   protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
      this.configurers.configureContentNegotiation(configurer);
   }
    ......
}
```

只是简单的覆盖父类的方法，那么我们在进入父类`WebMvcConfigurationSupport`看一下。

在`WebMvcConfigurationSupport`中我们发现了很多默认的@Bean定义，结合之前一篇文章，Debugger的时候发现在`org.springframework.web.servlet.DispatcherServlet#getHandler`方法中有两个`HandlerMapping` 具体的实现类。

一个是`RequestMappingHandlerMapping`,另一个是`BeanNameUrlHandlerMapping` 这两个Bean定义我们都可以在`WebMvcConfigurationSupport`中找到初始化的Bean。

```java
/**
 * Return a {@link BeanNameUrlHandlerMapping} ordered at 2 to map URL
 * paths to controller bean names.
 */
@Bean
public BeanNameUrlHandlerMapping beanNameHandlerMapping() {
   BeanNameUrlHandlerMapping mapping = new BeanNameUrlHandlerMapping();
   mapping.setOrder(2);
   mapping.setInterceptors(getInterceptors());
   mapping.setCorsConfigurations(getCorsConfigurations());
   return mapping;
}
```

```java
@Bean
public RequestMappingHandlerMapping requestMappingHandlerMapping() {
   RequestMappingHandlerMapping mapping = createRequestMappingHandlerMapping();
   mapping.setOrder(0);
   mapping.setInterceptors(getInterceptors());
   mapping.setContentNegotiationManager(mvcContentNegotiationManager());
   mapping.setCorsConfigurations(getCorsConfigurations());

   PathMatchConfigurer configurer = getPathMatchConfigurer();

   Boolean useSuffixPatternMatch = configurer.isUseSuffixPatternMatch();
   if (useSuffixPatternMatch != null) {
      mapping.setUseSuffixPatternMatch(useSuffixPatternMatch);
   }
   Boolean useRegisteredSuffixPatternMatch = configurer.isUseRegisteredSuffixPatternMatch();
   if (useRegisteredSuffixPatternMatch != null) {
      mapping.setUseRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch);
   }
   Boolean useTrailingSlashMatch = configurer.isUseTrailingSlashMatch();
   if (useTrailingSlashMatch != null) {
      mapping.setUseTrailingSlashMatch(useTrailingSlashMatch);
   }

   UrlPathHelper pathHelper = configurer.getUrlPathHelper();
   if (pathHelper != null) {
      mapping.setUrlPathHelper(pathHelper);
   }
   PathMatcher pathMatcher = configurer.getPathMatcher();
   if (pathMatcher != null) {
      mapping.setPathMatcher(pathMatcher);
   }

   return mapping;
}
```

同理，实现默认的`HandlerAdapter`接口的类也是在这里定义的。这里就不多做阐述，感兴趣的小伙伴可以自己去看一下源码。



**其实SpringMvc默认的组件加载就是在@EnableWebMvc这个注解标注在某个@Configuration 类上时，通过DelegatingWebMvcConfiguration这个类来加载的，然后在Servlet中组装**



## 注入组件

了解了如何加载，那么这些组件是何时注入的呢? 其实是在刷新上下文成功后注入的。

具体方法流程是

`org.springframework.web.servlet.DispatcherServlet#onRefresh`->`org.springframework.web.servlet.DispatcherServlet#initStrategies`



```java
@Override
protected void onRefresh(ApplicationContext context) {
   initStrategies(context);
}
```

```java
/**
 * Initialize the strategy objects that this servlet uses.
 * <p>May be overridden in subclasses in order to initialize further strategy objects.
 */
protected void initStrategies(ApplicationContext context) {
   initMultipartResolver(context);
   initLocaleResolver(context);
   initThemeResolver(context);
   initHandlerMappings(context);
   initHandlerAdapters(context);
   initHandlerExceptionResolvers(context);
   initRequestToViewNameTranslator(context);
   initViewResolvers(context);
   initFlashMapManager(context);
}
```



其实注入的方式都是一样的，我们就打开其中一个注入方法看一下细节`#initHandlerMappings()`

```java
/**
 * Initialize the HandlerMappings used by this class.
 * <p>If no HandlerMapping beans are defined in the BeanFactory for this namespace,
 * we default to BeanNameUrlHandlerMapping.
 */
private void initHandlerMappings(ApplicationContext context) {
   this.handlerMappings = null;

   if (this.detectAllHandlerMappings) {
      // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
      Map<String, HandlerMapping> matchingBeans =
            BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
      if (!matchingBeans.isEmpty()) {
         this.handlerMappings = new ArrayList<>(matchingBeans.values());
         // We keep HandlerMappings in sorted order.
         AnnotationAwareOrderComparator.sort(this.handlerMappings);
      }
   }
   else {
      try {
         HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
         this.handlerMappings = Collections.singletonList(hm);
      }
      catch (NoSuchBeanDefinitionException ex) {
         // Ignore, we'll add a default HandlerMapping later.
      }
   }

   // Ensure we have at least one HandlerMapping, by registering
   // a default HandlerMapping if no other mappings are found.
   if (this.handlerMappings == null) {
      this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
      if (logger.isDebugEnabled()) {
         logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
      }
   }
}
```

在这里最主要的就是 

```java
  Map<String, HandlerMapping> matchingBeans =
        BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
```
这一段代码，这段代码就是通过应用上下文找到所有实现`HandlerMapping`接口的Bean并装载到`DispatcherServlet`中。



## 自定义组件-- WebMvcConfigurer 

在上面的`DelegatingWebMvcConfiguration`这个类中我们发现有一个方法那就是

```java
@Autowired(required = false)
public void setConfigurers(List<WebMvcConfigurer> configurers) {
   if (!CollectionUtils.isEmpty(configurers)) {
      this.configurers.addWebMvcConfigurers(configurers);
   }
}
```

这个方法就是加载自定义配置组件的类方法。从这个方法的参数中我们可以看出，所有的自定义组件必须实现`org.springframework.web.servlet.config.annotation.WebMvcConfigurer`接口。

下面来让我们实现一个简单的自定义组件。让我们添加一个在执行`HandlerAdapter` 方法之前的拦截器。

简单的代码如下，该代码是在上一篇的配置类的新增的。

```java
/**
 * Spring Mvc 配置
 */
@Configuration   //配置
@EnableWebMvc    //激活组件并自动装配
@ComponentScan(basePackages = "com.web")
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     *     <bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
     *         <property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/>
     *         <property name="prefix" value="/WEB-INF/jsp/"/>
     *         <property name="suffix" value=".jsp"/>
     *     </bean>
     * @return
     */
    @Bean
    public ViewResolver viewResolver() {

        InternalResourceViewResolver internalResourceViewResolver = new InternalResourceViewResolver();
        internalResourceViewResolver.setViewClass(JstlView.class);
        internalResourceViewResolver.setPrefix("/WEB-INF/jsp/");
        internalResourceViewResolver.setSuffix(".jsp");
        return internalResourceViewResolver;
    }


    //增加自定义拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                System.out.println("---------自定义拦截器拦截-------");
                return true;
            }
        });
    }

}
```

同时我们改一下controller代码。

```java
@Controller
public class HelloWorldController {
    @RequestMapping("/index")
    public String index() {
        System.out.println("执行HelloWorldController中的index()方法");
        return "index";
    }
}
```

自定义完成之后接下来只需要跑一下程序，就可以看到自定义的组件是否加载到了容器之中，并成功拦截。

拦截器结果.png





我们已经简单的实现了一个自定义组件,我们可以通过根据业务需要覆盖`WebMvcConfigurer`接口中的默认方法来丰富我们的项目。