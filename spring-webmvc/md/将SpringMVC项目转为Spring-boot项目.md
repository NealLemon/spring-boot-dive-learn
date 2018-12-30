# 将SpringMVC项目转为Spring-boot项目

本文是基于慕课网小马哥的 《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。

## 前言

在 [理解SpringMvc架构以及流程](https://www.jianshu.com/p/6433a07f909c)里我们使用SpringMVC实现了简单的页面跳转,现在让我们将之前的demo转变为Spring-boot下,看看Spring-boot的简化操作。

## 目录结构以及代码

**代码大致结构**

转Spring-boot目录结构.png



**具体代码**

1.WebMvcConfig.java

```java
/**
 * Spring 拦截器 配置
 */
@Configuration   //配置
public class WebMvcConfig implements WebMvcConfigurer {

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



2.HelloWorldController.java

```java
/**
 * 简单controller
 */
@Controller
public class HelloWorldController {
    @RequestMapping("/index")
    public String index() {
        System.out.println("执行HelloWorldController中的index()方法");
        return "index";
    }
}
```



3.SpringBootWebMvcBootStrap.java

```java
/**
 * Spring-boot 启动引导类
 */
@SpringBootApplication
public class SpringBootWebMvcBootStrap {


    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebMvcBootStrap.class,args);

    }
}
```



4.application.properties

```properties
spring.mvc.view.prefix=/WEB-INF/jsp/
spring.mvc.view.suffix=.jsp
```



5./WEB-INF/jsp/index.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<p>Hello World</p>

</body>
</html>
```



以上就是简单Demo的所有代码，我们可以看到 与SpringMVC相比确实少了很多东西。不需要`@EnableWebMvc`注解,也不需要配置`ViewResolver` ，只需要配置`application.properties` 将视图路径简单配置一下就可以了，Spring-boot通过内容协调相关组件，会自动匹配出JstlView实现类去渲染JSP。



## 调试与问题

代码都码完了，那么就让我们简单启动一下Demo,这里会遇到一个问题，是IDEA自身启动web项目时处理的问题。稍后我们会看到。

**启动**

运行console.png



我们可以看到,访问路径`{/index}`的映射已经被加载到了容器之中，那么让我们访问一下http://localhost:8080/index 看看是否跳转到了我们定义的index.jsp内。

index结果.png



可以看到我们的页面并没有被找到，那么原因是什么，难道我们的Demo有问题，答案当然不是了。是因为IDEA自身编译运行时处理出现的这个问题。那么具体原因出在哪呢。我通过Debugger的方式对应之前的SpringMVC的Demo发现了问题。

主要问题就是SpringMVC和Springboot两个demo在运行时的上下文也就是 `ApplicationContext` 对应的路径导致的问题。

**主要关注 `org.apache.catalina.core.ApplicationDispatcher#doForward` 这个方法**

1.在SpringMVC下的调试

tomcat下的context路径.png

我们发现在外置的tomcat下启动 context路径 很明确，并且通过我们电脑寻址，可以找到对应的目录以及JSP文件。

tomcat下文件.png



2.在Spring-boot下的调试

springboot下的context路径.png



我们发现在我们使用Spring-boot启动tomcat时，IDEA会将项目指向一个临时目录，那么这个目录里存放着什么呢？

临时文件下夹下的路径.png



答案是什么都没有，这也就是为什么，我们找不到对应的JSP的原因。



## 结论

虽然Spring-boot简化了很多Spring的配置，但是也有很多细节问题是编译器避免不了的，需要我们细心去发现，如果我们把Spring-boot项目打成jar包，通过命令启动的话，就不会出现这种问题了。