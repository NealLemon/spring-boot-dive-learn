## Spring--视图内容协商（四）

本文是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。 

​	学习了这么久视图内容协商处理，下面让我们来自定义 方法参数解析器和返回值解析器来 处理传参为 `Properties` 类型的请求。

### 大纲

- 需求说明
- 定义一个调试类
- 自定义方法参数解析器
- 自定义返回值解析器
- 整合以及调试

### 需求说明

​	实现 `Content-Type` 为 `text/properties`  媒体类型并且传参是 `Properites` 的文本（例如 `name=neal`）的post 请求。



### 定义一个`PropertiesController`调试类

```java
/**
 * 自定义实现 Content-Type 为 text/properties
 */
@Controller
public class PropertiesController {

    /**
     * 接受并返回传入的properties
     * @param properties
     * @return
     */
    @PostMapping(value = "/self/properties",
    consumes = "text/properties;charset=UTF-8")
    public Properties resolveProperties(Properties properties) {
        return properties;
    }
}
```

 这里就是很简单的一个Controller,在方法中只接受`Content-Type` 是 `text/properties` 的POST请求，同时 入参类型我们设置为 `Properties`。

### 自定义方法参数解析器

​	在我们自己实现一个自定义方法参数解析器时，我们最好先看一下Spring自带的方法参数解析器，感兴趣的小伙伴可以看一下`RequestResponseBodyMethodProcessor`  这也是上一篇讲解源码时所用到的解析器，由于它既是参数解析器又是返回值解析器，所以我们使用它来查看具体实现方法是再好不过了。话不多说，让我们先看一下实现一个自定义方法参数解析器的步骤。

具体步骤:

- 定义一个类并实现接口 `HandlerMethodArgumentResolver` 。

- 重写`supportsParameter()` 方法，使其支持`Properties` 类型。

- 重写`resolveArgument()` 方法，实现 Properties 格式请求内容，解析为 Properties 对象的方法参数。

  

  代码:

  ```java
  /**
   * 自定义 Properties 参数解析器
   */
  public class PropertiesHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
  
  
      /**
       * 是否支持参数解析
       * @param parameter
       * @return
       */
      @Override
      public boolean supportsParameter(MethodParameter parameter) {
          //这里判断方法中的入参类型 是否匹配Properties
          return Properties.class.equals(parameter.getParameterType());
      }
  
      /**
       * 处理并获取请求的值
       * @param parameter
       * @param mavContainer
       * @param webRequest
       * @param binderFactory
       * @return
       * @throws Exception
       */
      @Override
      public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
          //处理请求传入的值，并返回Properties 类型。
          Properties arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
          return arg;
      }
  
      /**
       * 参数读取
       * @param webRequest
       * @param parameter
       * @param nestedGenericParameterType
       * @return
       */
      private Properties readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter, Type nestedGenericParameterType) throws Exception {
          //获取原生的HttpServletRequest
          HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
          Assert.state(servletRequest != null, "No HttpServletRequest");
          //获取Spring定义的 ServletServerHttpRequest
          ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);
          //获取请求头中的 Content-Type
          MediaType contentType = inputMessage.getHeaders().getContentType();
          // 获取字符编码
          Charset charset = contentType.getCharset();
          // 当 charset 不存在时，使用 UTF-8
          charset = charset == null ? Charset.forName("UTF-8") : charset;
          // 获取字节流
          InputStream inputStream = inputMessage.getBody();
          //Properties的load方法需要一个实现Reader接口的字节流
          InputStreamReader reader = new InputStreamReader(inputStream, charset);
          //声明一个properties对象
          Properties properties = new Properties();
          // 加载字符流成为 Properties 对象
          properties.load(reader);
          reader.close();
          return properties;
      }
  }
  ```

  每一步的解释我都写在了注释里，相信大家都可以看懂。

  

  ### 自定义返回值解析器

  ​	自定义返回解析器也可以参考`RequestResponseBodyMethodProcessor`   中的实现，但是它里面的实现比较复杂，做了很多校验以及逻辑判断，我们只需要简单实现一个将返回值解析成`Properties` 类型的解析器就行了。

  具体步骤:

  - 定义一个类并实现接口 `HandlerMethodReturnValueHandler`。
  - 重写`supportsReturnType()` 方法，使其支持`Properties` 类型。
  - 重写`handleReturnValue()` 方法，实现 Properties 类型方法返回值，转化为 Properties 格式内容响应内容。

  代码:

```java
/**
 * 自定义Properties 返回值解析器
 */
public class PropertiesHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    /**
     * 是否支持返回值类型
     * @param returnType
     * @return
     */
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        //这里判断方法中的返回值类型 是否匹配Properties
        return Properties.class.equals(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        //这里这是为true,告诉Spring已经完成了所有的处理，不需要往下处理了
        mavContainer.setRequestHandled(true);
        //获取Spring定义的 ServletServerHttpRequest
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(webRequest.getNativeRequest(HttpServletRequest.class));
        //获取Spring定义的 ServletServerHttpResponse
        ServletServerHttpResponse response = new ServletServerHttpResponse(webRequest.getNativeResponse(HttpServletResponse.class));
        //获取响应头中的 Content-Type
        MediaType contentType = response.getHeaders().getContentType();
        //如果响应头为空
        if(contentType == null) {
            //将请求头中的Content-Type 设置成 响应头的Content-Type
            contentType = inputMessage.getHeaders().getContentType();
        }
        //设置响应头为请求头的Content-type
        response.getHeaders().setContentType(contentType);
        // 获取字符编码
        Charset charset = contentType.getCharset();
        // 当 charset 不存在时，使用 UTF-8
        charset = charset == null ? Charset.forName("UTF-8") : charset;

        //获取响应流
        OutputStream outputStream = response.getBody();
        // 字符输出流
        Writer writer = new OutputStreamWriter(outputStream, charset);
        // Properties 写入到字符输出流
        Properties properties = (Properties)returnValue;
        //响应回客户端
        properties.store(writer,"From Neal Self");
        writer.close();
    }
}
```

同样的每一步的解释我都写在了注释里，相信大家都可以看懂，至于Properties 写方法，可以参考一下API。

### 整合以及调试

##### 整合

在整合前需要说明一下，正常来说整合自定义的参数解析器和返回值解析器只需要继承`org.springframework.web.servlet.config.annotation.WebMvcConfigurer`并 实现 `addArgumentResolvers()` 和 `addReturnValueHandlers()` 把对应的解析器添加到容器中就可以了 PS：都是在末尾添加。

```java
/**
 * Add resolvers to support custom controller method argument types.
 * <p>This does not override the built-in support for resolving handler
 * method arguments. To customize the built-in support for argument
 * resolution, configure {@link RequestMappingHandlerAdapter} directly.
 * @param resolvers initially an empty list
 */
default void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
}

/**
 * Add handlers to support custom controller method return value types.
 * <p>Using this option does not override the built-in support for handling
 * return values. To customize the built-in support for handling return
 * values, configure RequestMappingHandlerAdapter directly.
 * @param handlers initially an empty list
 */
default void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
}
```

但是我们的`Properties` 类型的参数是可以被 之前Spring内置的解析器解析，所以在解析器顺序迭代时，就不会被我们自定义的解析器解析。所以在这里参照小马哥的方法来实现，将我们自定义的参数解析器都放在第一个。

具体代码和注释如下:

```java
/**
 * 视图协商相关配置
 */
@Configuration   //配置
public class RestWebMvcConfig implements WebMvcConfigurer {


    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    //在依赖注入完成后被自动调用
    @PostConstruct
    public void init() {
        // 获取当前 RequestMappingHandlerAdapter 所有的 Resolver 对象
        List<HandlerMethodArgumentResolver> resolvers = requestMappingHandlerAdapter.getArgumentResolvers();
        List<HandlerMethodArgumentResolver> newResolvers = new ArrayList<>(resolvers.size() + 1);
        // 添加 PropertiesHandlerMethodArgumentResolver 到集合首位
        newResolvers.add(new PropertiesHandlerMethodArgumentResolver());
        // 添加 已注册的 Resolver 对象集合
        newResolvers.addAll(resolvers);
        // 重新设置 Resolver 对象集合
        requestMappingHandlerAdapter.setArgumentResolvers(newResolvers);

        // 获取当前 HandlerMethodReturnValueHandler 所有的 Handler 对象
        List<HandlerMethodReturnValueHandler> handlers = requestMappingHandlerAdapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>(handlers.size() + 1);
        // 添加 PropertiesHandlerMethodReturnValueHandler 到集合首位
        newHandlers.add(new PropertiesHandlerMethodReturnValueHandler());
        // 添加 已注册的 Handler 对象集合
        newHandlers.addAll(handlers);
        // 重新设置 Handler 对象集合
        requestMappingHandlerAdapter.setReturnValueHandlers(newHandlers);

    }

}
```

##### 调试

代码已经介绍完了。接下来让我们启动Spring-boot，来试试我们刚刚写的解析器是否起到了作用。

打开Postman，并设置请求头和body。

p1.png

p2.png

发送请求并查看结果

p3.png



我们可以看到，我们自定义的解析器已经成功解析并正确返回了。



### 总结

​	通过这么久的学习，对于内容协商有了一定的了解，并且可以自定义一个方法参数解析器和返回值解析器。Spring博大精深，还需要继续努力探索。