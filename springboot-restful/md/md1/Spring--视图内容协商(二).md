

# Spring--视图内容协商(二)

本文是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。 

上一篇 [Spring--视图内容协商(一)](https://www.jianshu.com/p/a743fc262f49)  讲解了如何配置内容协商以及spring-boot是如何配置关联匹配策略的。现在让我们来自己走一遍协商流程,加深印象。

由于现在主要都是Restful API形式的请求，就主要把`HeaderContentNegotiationStrategy ` 这个默认加载的视图协商处理简单的记录和解释一下。

这里我们就做一个简单的demo,根据请求头的格式不同来返回不同的渲染引擎模版,如果是Accept:text/xml 则返回JSP,如果是Accept:text/html 则返回 thymeleaf。

 ## 代码

针对上一篇的代码有所修改，索性全部贴上。

#### 项目目录

目录.png

#### pom.xml

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

    <!-- Thymeleaf 模板 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
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

#### WebMvcConfig

```java
/**
 * 视图协商相关配置
 */
@Configuration   //配置
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 配置新的JSP视图解析
    */
    @Bean
    public ViewResolver myViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setOrder(Ordered.LOWEST_PRECEDENCE-10);
        viewResolver.setContentType("html/xml;charset=UTF-8");
        return viewResolver;
    }

/*    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorParameter(true).favorPathExtension(true);

    }*/
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

#### HelloWorldController

```java
/**
 * 简单controller
 */
@Controller
public class HelloWorldController {
    @RequestMapping("/")
    public String index() {
        System.out.println("执行HelloWorldController中的index()方法");
        return "index";
    }
}
```

#### 引导类SpringBootRestfulBootStrap

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

#### application.properties

```properties
spring.mvc.view.prefix=/WEB-INF/jsp/
spring.mvc.view.suffix=.jsp
spring.thymeleaf.prefix = classpath:/templates/thymeleaf/
spring.thymeleaf.suffix = .html
spring.thymeleaf.cache = false
```

#### thymeleaf---index.html

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>thymeleaf Page</title>
</head>
<body>
<div>hello world</div>
</body>
</html>
```

#### JSP---index.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>JSP Page</title>
</head>
<body>
<p>Hello World</p>

</body>
</html>
```

## 视图协商效果

在探讨spring是如何进行请求头协商策略（`HeaderContentNegotiationStrategy `）的之前，我们先来看一下协商结果。

使用postMan测试效果。

1.请求头格式是是 text/html 。让我们看一下返回的是不是thymeleaf的index.html。

postman-thyemleaf.png

2.请求头格式是是 text/html 。让我们看一下返回的是不是jsp的index.jsp。

postman-jsp.png



我们可以看到,结果符合预期，那么接下来让我们深入的理解一下 Spring的视图协商流程。

## 理解视图协商流程

在具体理解`HeaderContentNegotiationStrategy `流程之前，我们要先要明白两个关键点。

-  HTTP Accept 请求头 与 ViewResolver的 Content-Type 匹配
- 匹配规则顺序
  - `ViewResolver` 匹配
  - `MediaType` 匹配

##### HTTP Accept 请求头 与 ViewResolver的 Content-Type 匹配

  对于HTTP Accept 请求头相信大家都知道在此就不多做介绍了,对于`ViewResolver`的`Content-Type` 在这里我们就拿我们自定义的视图解析器来看一下就明白了。

com.web.configuration.WebMvcConfig#myViewResolver

```java
@Bean
public ViewResolver myViewResolver() {
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setViewClass(JstlView.class);
    viewResolver.setPrefix("/WEB-INF/jsp/");
    viewResolver.setSuffix(".jsp");
    //视图解析器顺序
    viewResolver.setOrder(Ordered.LOWEST_PRECEDENCE-10);
    //配置视图解析器的ContentType
    viewResolver.setContentType("html/xml;charset=UTF-8");
    return viewResolver;
}
```

我们可以看到 在自定义的视图解析器中，我们定义了JstlView 视图渲染引擎，也就是JSP 的渲染引擎。

重点是我们将 自定义的视图解析器的Content-Type 的内容设置为 `html/xml;charset=UTF-8` 。这样就可以通过浏览器请求的Accept 类型进行匹配。具体是如何进行匹配的稍后我们一起来看源码。

而对于匹配规则,我们稍后看一下源码就可以理解了。

#### 视图协商源码简单解读

在之前已经介绍了SpringMvc的架构流程，我们在这里直接看重点，具体想知道如何跳进这段源码的，可以看看之前的博文。

我们先用浏览器进行简单的请求 localhost:8080.

首先我们来看一下`org.springframework.web.servlet.DispatcherServlet#resolveViewName`

l1.png

l2.png

我们可以看到 在这里视图解析器有6个 而排在第一个的就是我们的视图协商解析器， 在上一篇文章也介绍过，这个解析器中 包含了非他意外的所有解析器。我们接着往下看，进入到`resolveViewName`方法中。



在这个方法中，首先是通过 `getMediaTypes` 获取请求头的所有媒体类型。我们来细看一下这个`getMediaTypes`方法。步骤解释已在注释里说明。

```java
@Nullable
protected List<MediaType> getMediaTypes(HttpServletRequest request) {
   Assert.state(this.contentNegotiationManager != null, "No ContentNegotiationManager set");
   try {
      ServletWebRequest webRequest = new ServletWebRequest(request);
       //1.获取媒体类型
      List<MediaType> acceptableMediaTypes = this.contentNegotiationManager.resolveMediaTypes(webRequest);
       /2./获取程序可生成的媒体类型 在这里我们默认为所有
      List<MediaType> producibleMediaTypes = getProducibleMediaTypes(request);
       
      Set<MediaType> compatibleMediaTypes = new LinkedHashSet<>();
      //3.针对可生成的媒体类型进行匹配
      for (MediaType acceptable : acceptableMediaTypes) {
         for (MediaType producible : producibleMediaTypes) {
            if (acceptable.isCompatibleWith(producible)) {
               compatibleMediaTypes.add(getMostSpecificMediaType(acceptable, producible));
            }
         }
      }
      //4.最终生成可接受的请求头
      List<MediaType> selectedMediaTypes = new ArrayList<>(compatibleMediaTypes);
      MediaType.sortBySpecificityAndQuality(selectedMediaTypes);
      if (logger.isDebugEnabled()) {
         logger.debug("Requested media types are " + selectedMediaTypes + " based on Accept header types " +
               "and producible media types " + producibleMediaTypes + ")");
      }
      return selectedMediaTypes;
   }
   catch (HttpMediaTypeNotAcceptableException ex) {
      return null;
   }
}
```

在这里我们需要注意的是，注释1 的获取媒体类型的方法。这个方法是通过Spring 配置的策略来遍历获取最合适的媒体类型集合。

l3.png 

我们顺便来看一下 `org.springframework.web.accept.HeaderContentNegotiationStrategy#resolveMediaTypes`的方法就明白这个策略就是来匹配请求头的策略。

```java
@Override
public List<MediaType> resolveMediaTypes(NativeWebRequest request)
      throws HttpMediaTypeNotAcceptableException {

    //获取请求头的所有媒体类型字符串数组
   String[] headerValueArray = request.getHeaderValues(HttpHeaders.ACCEPT);
   if (headerValueArray == null) {
      return MEDIA_TYPE_ALL_LIST;
   }

   List<String> headerValues = Arrays.asList(headerValueArray);
   try {
      //解析成Spring 自己定义的媒体类型结合 
      List<MediaType> mediaTypes = MediaType.parseMediaTypes(headerValues);
      MediaType.sortBySpecificityAndQuality(mediaTypes);
      return !CollectionUtils.isEmpty(mediaTypes) ? mediaTypes : MEDIA_TYPE_ALL_LIST;
   }
   catch (InvalidMediaTypeException ex) {
      throw new HttpMediaTypeNotAcceptableException(
            "Could not parse 'Accept' header " + headerValues + ": " + ex.getMessage());
   }
}
```



媒体类型获取完成后接下来我们就要获取最佳的视图匹配。我们接着往下看 我们通过`ViewName` 来获取 候选的视图。

l4.png

l5.png



选出候选的视图解析器之后，那么重中之重的地方就来了，那就是选择最佳的视图解析器。我们来看一下 `org.springframework.web.servlet.view.ContentNegotiatingViewResolver#getBestView` 的代码

l6.png

l7.png

重要的代码部分我已经拿红框圈出，在这里我们可以一步一步的了解到 Spring 是如何选出最佳的视图解析器的。

l8.png

我们可以看到通过两层校验 

- 第一步是判断视图解析器是设置了 `ContentType` 
- 第二步是通过 `isCompatibleWith()` 方法来进行校验匹配。   

第一个视图解析器就是我们自定义的 JSP视图解析器，而 我们请求的是 `mediaType:"text/html"`,而我们自定义的JSP视图解析器中的 Content-Type 是 `html/xml;charset=UTF-8` 因此不匹配。

那么我们来看一下第二个 视图解析器 也就是 Thymeleaf解析器 。

l9.png

我们可以看到 请求的 `mediaType:"text/html"` 和 Thymeleaf解析器中的  Content-Type `html/html;charset=UTF-8` 可以匹配上,那么 Spring就会选定它为最佳的视图解析器。

l10.png



## 总结

  Spring 基本的内容协商操作流程已经介绍完了。可以参照上一篇的 流程图来看 ，相信会理解的更快。 内容协商的错综复杂关系 需要一定时间的研究才能初步理解，相信只要坚持，理解Spring的脚步就越来越近了。



[DEMO地址](https://github.com/NealLemon/spring-boot-dive-learn/tree/master/springboot-restful)

