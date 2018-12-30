# Spring-boot时代的完全自动装配

本文是基于慕课网小马哥的 《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。

之前我们在Spring项目中使用`@EnableWebMvc`来实现自动装配，接下来我们来看一下spring-boot是如何自动装配的。

## 概要

- 自动装配 SpringMvc-> ` DispatcherServlet ` :  Spring-boot->`DispatcherServletAutoConfiguration`
- 导入Spring配置: SpringMvc->`@EnableWebMvc`:Spring-boot->`WebMvcAutoConfiguration`
- Servlet容器 :Spring-boot->` ServletWebServerFactoryAutoConfiguration`

## DispatcherServletAutoConfiguration

在SpringMVC中我们使用`org.springframework.web.servlet.DispatcherServlet`。

而在Spring-boot中我们装配这些组件那么就会使用`org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration` 。

在`DispatcherServletAutoConfiguration` 中自动装配的实现比`DispatcherServlet`要复杂很多。

1.首先这个自动装配类需要很多前置的条件装配以及判断。

```java
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
@AutoConfigureAfter(ServletWebServerFactoryAutoConfiguration.class)
@EnableConfigurationProperties(ServerProperties.class)
public class DispatcherServletAutoConfiguration {
```

2.由`org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration.DispatcherServletConfiguration`声明`DispatcherServlet`，根据`spring-boot-autoconfigure/META-INF/spring-configuration-metadata.json`的默认配置去声明。

```java
@Bean(name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
public DispatcherServlet dispatcherServlet() {
   DispatcherServlet dispatcherServlet = new DispatcherServlet();
   dispatcherServlet.setDispatchOptionsRequest(
         this.webMvcProperties.isDispatchOptionsRequest());
   dispatcherServlet.setDispatchTraceRequest(
         this.webMvcProperties.isDispatchTraceRequest());
   dispatcherServlet.setThrowExceptionIfNoHandlerFound(
         this.webMvcProperties.isThrowExceptionIfNoHandlerFound());
   return dispatcherServlet;
}
```

3.由`org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration`去注册刚刚生成的`DispatcherServlet`

```java
@Bean(name = DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
@ConditionalOnBean(value = DispatcherServlet.class, name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
public DispatcherServletRegistrationBean dispatcherServletRegistration(
      DispatcherServlet dispatcherServlet) {
   DispatcherServletRegistrationBean registration = new DispatcherServletRegistrationBean(
         dispatcherServlet, this.serverProperties.getServlet().getPath());
   registration.setName(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
   registration.setLoadOnStartup(
         this.webMvcProperties.getServlet().getLoadOnStartup());
   if (this.multipartConfig != null) {
      registration.setMultipartConfig(this.multipartConfig);
   }
   return registration;
}
```



在这里需要注意，这里的注册使用了`DispatcherServletRegistrationBean`我们通过向上查找父类呢，我们发现了这个SPI接口`org.springframework.boot.web.servlet.ServletContextInitializer` 这个接口就是当我们容器启动时，回调这个接口，从而将我们注册的servlet全部注入的容器中。

4.剩下的方法则是对默认的`Servlet`以及`DispatcherServletRegistrationBean`的条件匹配类。

## WebMvcAutoConfiguration

在SpringMVC中我们使用`@EnableWebMvc` 自动装配基于JavaBean机制配置的默认的组件如`HandlerMapping`,`HandlerAdapter`,`ViewResolver`,`HandlerExceptionResolver`等。

在Spring-boot中我们使用`org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration`来进行以上的组件装配。

但是和`@EnableWebMvc`不同的是，在Spring-boot中作了许多条件判断。

```java
@Configuration
//WEB类型判断
@ConditionalOnWebApplication(type = Type.SERVLET)
//API的判断
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })
//当没有WebMvcConfigurationSupport时，才加载
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
//加载的绝对顺序
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
//加载的相对顺序
@AutoConfigureAfter({ DispatcherServletAutoConfiguration.class,
      ValidationAutoConfiguration.class })
public class WebMvcAutoConfiguration {
    ....
}
```

在这里需要注意一下为什么要使用`@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)` 。因为在SpringMvc中 `@EnableWebMvc` 使用注解驱动的方式 使用`DelegatingWebMvcConfiguration`这个类来进行驱动加,他的父类就是 `WebMvcConfigurationSupport` 

```java
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport
```

换句话说，SpringMVC就是使用`WebMvcConfigurationSupport` 来进行组件的自动装配的。所以在`WebMvcAutoConfiguration`中，我们会先进行判断，容器中是否有 `WebMvcConfigurationSupport`  这个类。防止组件重复自动装配。 

我们来看在`WebMvcAutoConfiguration`中自动装配的几个重要组件。

```java
//内容协调
@Override
public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    ...
}

//默认的ViewResolver
@Bean
@ConditionalOnMissingBean
public InternalResourceViewResolver defaultViewResolver() {
    ...
}
```

具体的就不一一举例了，大体就是装配了spring-boot需要的组件。

## ServletWebServerFactoryAutoConfiguration

这个`org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration`是spring-boot启动容器的自动装配类。

```java
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(ServletRequest.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(ServerProperties.class)
@Import({ ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class,
      ServletWebServerFactoryConfiguration.EmbeddedTomcat.class,
      ServletWebServerFactoryConfiguration.EmbeddedJetty.class,
      ServletWebServerFactoryConfiguration.EmbeddedUndertow.class })
public class ServletWebServerFactoryAutoConfiguration {
    ...
}
```

我们看到这个类也有注解驱动，同时我们从类名就可以看到，需要加载的是容器Tomcat,Jetty,Undertow。

简单的看一`ServletWebServerFactoryConfiguration.EmbeddedTomcat.class` ，就是利用各种工厂来创建所需要的容器



```java
class ServletWebServerFactoryConfiguration {

   @Configuration
   @ConditionalOnClass({ Servlet.class, Tomcat.class, UpgradeProtocol.class })
   @ConditionalOnMissingBean(value = ServletWebServerFactory.class, search = SearchStrategy.CURRENT)
   public static class EmbeddedTomcat {

      @Bean
      public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
         return new TomcatServletWebServerFactory();
      }

   }
```

我们在进一步看一下`ServletWebServerFactoryAutoConfiguration`类中的方法。

```java
@Bean
public ServletWebServerFactoryCustomizer servletWebServerFactoryCustomizer(
      ServerProperties serverProperties) {
   return new ServletWebServerFactoryCustomizer(serverProperties);
}

@Bean
@ConditionalOnClass(name = "org.apache.catalina.startup.Tomcat")
public TomcatServletWebServerFactoryCustomizer tomcatServletWebServerFactoryCustomizer(
      ServerProperties serverProperties) {
   return new TomcatServletWebServerFactoryCustomizer(serverProperties);
}
```

无非就是根据外部化配置，配置所要初始化容器如tomcat,jetty等。

在类中还有一个静态内部类`ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar`。

```java
/**
 * Registers a {@link WebServerFactoryCustomizerBeanPostProcessor}. Registered via
 * {@link ImportBeanDefinitionRegistrar} for early registration.
 */
public static class BeanPostProcessorsRegistrar
      implements ImportBeanDefinitionRegistrar, BeanFactoryAware {
    ...
}
```

用来配置容器的Bean处理类以及各种定义Bean的内容的注册接口。



## 总结

大致上Spring-boot的三大核心跟SpringMVC没什么本质上的区别。只是需要配置的东西更丰富了一些。