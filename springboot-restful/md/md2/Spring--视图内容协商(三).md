# Spring--视图内容协商(三)

本文是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。 

上两篇文章，简单介绍了一下Spring的视图内容协商。接下来我们针对 REST 内容协商做一下介绍。

### 大纲

- 理解REST请求媒体类型
- REST内容协商流程
- REST内容协商源码分析

#### 理解REST请求媒体类型

##### 理解解析请求的媒体类型

在我们使用Spring开发的时候，相信 `@RequestMapping` 这个注解再也熟悉不过了，相信使用Restful API 接口形式开发的小伙伴们都避免不了设置API的媒体类型 比如 `Accept`和 `Content-Type`  ，在前一篇我们也说过 Spring 通过 `ContentNegotiationManager` 的 `ContentNegotiationStrategy` 解析请求中的媒体类型。

以  `HeaderContentNegotiationStrategy ` 为例

- 如果解析成功，则返回合法的`MediaType`集合。
- 否则，返回`MediaType.ALL`默认的媒体类型，也就是 `*/*`

在这里我们可以看一下`HeaderContentNegotiationStrategy ` 源码，具体解释已经在注释中给出

```java
@Override
public List<MediaType> resolveMediaTypes(NativeWebRequest request)
      throws HttpMediaTypeNotAcceptableException {
   //获取 Accept的 媒体类型的字符串数组
   String[] headerValueArray = request.getHeaderValues(HttpHeaders.ACCEPT);
   //如果为空 则返回 全部类型 也就是 */*
   if (headerValueArray == null) {
      return MEDIA_TYPE_ALL_LIST;
   }

   List<String> headerValues = Arrays.asList(headerValueArray);
   try {
      //转换为Spring 内置媒体类型集合 
      List<MediaType> mediaTypes = MediaType.parseMediaTypes(headerValues);
       //最佳媒体类型排序
      MediaType.sortBySpecificityAndQuality(mediaTypes);
      return !CollectionUtils.isEmpty(mediaTypes) ? mediaTypes : MEDIA_TYPE_ALL_LIST;
   }
   catch (InvalidMediaTypeException ex) {
      throw new HttpMediaTypeNotAcceptableException(
            "Could not parse 'Accept' header " + headerValues + ": " + ex.getMessage());
   }
}
```



当然 在 `ContentNegotiationManager`  中也会有判断 

```java
@Override
public List<MediaType> resolveMediaTypes(NativeWebRequest request) throws HttpMediaTypeNotAcceptableException {
    //循环遍历协商处理策略
   for (ContentNegotiationStrategy strategy : this.strategies) {
      //获取媒体类型 比如 HeaderContentNegotiationStrategy 
      List<MediaType> mediaTypes = strategy.resolveMediaTypes(request);
       //如果媒体类型是 默认的所有 即 */* 则跳过 
      if (mediaTypes.equals(MEDIA_TYPE_ALL_LIST)) {
         continue;
      }
      return mediaTypes;
   }
   //如果都不满足 则最后返回 默认所有 即*/*
   return MEDIA_TYPE_ALL_LIST;
}
```

对比上面的源码我们可以看到 Spring 做了很多层校验 来判断媒体类型是否为空。



##### 理解可生成的媒体类型

使用`@RequestMapping.produces()`  属性 来指定MediaType 类型集合  **影响** 浏览器响应头 Content-Type 媒体类型映射。

- 如果 `@RequestMapping.produces()` 存在，返回指定 MediaType 列表。
- 否则，返回已注册的 `HttpMessageConverter` 列表中支持的 MediaType 列表。
- 如果该列表与请求的媒体类型兼容，执行第一个兼容 `HttpMessageConverter` 的实现，默认
  `@RequestMapping#produces` 内容到响应头 Content-Type
  否则，抛出 `HttpMediaTypeNotAcceptableException` , HTTP Status Code : 415

这段源码会在稍后的源码分析中做详解。



##### 理解可消费的媒体类型

使用`@RequestMapping#consumes` 属性，来设置兼容的MediaType 类型集合 **过滤** 请求头 Content-Type 媒体类型映射。

- 如果请求头 Content-Type 媒体类型兼容 `@RequestMapping.consumes()` 属性，执行该 `HandlerMethod`
- 否则 `HandlerMethod` 不会被调用。

这段源码会在稍后的源码分析中做详解。



#### REST内容协商流程

在讲述REST协商流程之前，我们先来了解一下 对于REST内容协商非常关键的两个解析器

- 处理方法参数解析器（`HandlerMethodArgumentResolver `）
  - 用于 HTTP 请求中解析 `HandlerMethod` 参数内容
- 处理方法返回值解析器(`HandlerMethodReturnValueHandler`)
  - 用于 `HandlerMethod` 返回值解析为 HTTP 响应内容

##### 流程图

对于协商流程，在这里贴一张图，流程大体就可以理解了，在结合下面的源码分析，相信很快就可以明白Spring的视图协商流程。

总体流程图.png



针对 上述第10步 转化HTTP消息 的详细流程图

详细流程图.png



##### 调试前的代码准备

在这里需要增加一个简单的`User`对象以及对应请求的`Controller`

User.java

```java
/**
 * 用户对象
 */
public class User {

    private Long id;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

UserRestController.java

```java
@RestController
public class UserRestController {


    //最终相应回浏览器的Content-Type 是 produces 的内容
    @PostMapping(value = "/user",
            consumes = "application/json;charset=UTF-8",
            produces = "application/json;charset=GBK")
    public User user(@RequestBody User user) {
        return user;
    }
}
```



添加完这两个类之后，让我们启动一下Spring-boot项目。

我们可以对比上面的总体流程图来对源码进行解读。我们使用PostMan来发送一个简单的请求。

postman1.png

postman2.png



##### 首先我们先打开`DispatcherServlet#doDispatch` 方法。我们来关注下面两个方法

st2.png

##### 首先我们先来看一下 步骤 2和3的对应方法

st1.png

##### 接下来让我们进入到 步骤 4的对应方法中

st3.png

##### 接下来就是我们的重头戏就是调用`HandlerMethod`，在这个阶段就包括了剩下的步骤，也就是步骤5~10。

st4.png

##### 我们一步一步的往方法里进。

st5.png

##### 我们接着进入到 `RequestMappingHanlderAdapter`  中的 `handleInternal` 方法。

st7.png

st8.png

st9.png



##### 我们接着进入到 `invokeAndHandle` 方法 

st10.png

st11.png

##### 我们详细看一下如何解析方法参数的。

st12.png

##### 这里我们重点看一下 `argumentResolvers.supportsParameter(parameter)`  这段代码 主要是判断 参数处理器是否支持解析传入的参数。 

st13.png

##### 判断完某个解析器（`RequestResponseBodyMethodProcessor`）是否支持解析，接下来就是具体解析的操作了。

st14.png

##### 进入到 `resolveArgument`方法

st16.png

st15.png



##### 在这里我们已经获取到了 入参的值，我们回到最初调用的方法然后反射调用方法。

st17.png

st18.png

st19.png

##### 至此 我们步骤8之前的已经讲解完了。下面我们讲解一下返回值解析。我们重新回到`org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod#invokeAndHandle` 方法中。

st20.png

##### 进入`handleReturnValue` 方法。

st21.png

##### 我们来看一下 `selectHandler()` 这个方法

st22.png 

##### 选择好了返回值解析器 （`RequestResponseBodyMethodProcesser`），下面我们就看看如何解析返回值的。

##### 细心的小伙伴可以发现 这个解析器即是 方法参数解析器 又是 返回值解析器。这里就不多做解释了 这是因为他即继承`AbstractMessageConverterMethodArgumentResolver` 抽象类又实现了 `HandlerMethodReturnValueHandler` 接口。我们再进入到里面一层的`handleReturnValue` 

st23.png

st24.png



##### 接下来的内容就是详细流程图中的内容了我们从第7步开始看，我们通过媒体类型来匹配HttpMessageConverter。

st25.png

st26.png



##### 选择好转换器之后我们就可以进行转换了，我们看一下`MappingJackson2HttpMessageConverter` 是如何转换然后返回相应的。

st27.png~st30.png



##### 最后，在PostMan中显示返回结果

st31~st32.png



##### 通过响应头 我们也可以看到 `@RequestMapping.produces()` 的作用。



## 总结

  虽然协商逻辑以及流程比较繁琐，但是在我们使用Spring的时候，这些功能给了我们很大的便利，至于 最后Jackson是如何序列化的，这里就不详细说明了。不属于本次内容的范畴。  别的不多说，继续努力吧。





