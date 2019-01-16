# Springboot 2.0---WebFlux请求处理流程

  笔记是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。 

## 前言

  在了解了WebFlux核心组件之后，我们就该了解相应的请求流程了，在之前就写过几篇关于 SpringMvc 请求的流程笔记，如果之前有过了解的并且自己debugger 过的小伙伴，相信了解 WebFlux 的执行流程会很快。

  我们知道WebFlux支持两种请求模式 

- 注解驱动
- 函数式端点

接下来我们会重点讲一下函数式端点请求的具体流程，注解驱动由于跟SpringMVC很像，所以大家只要看一下流程图，基本就可以了解了。

## 注解驱动组件请求处理流程

### 流程图

下面我们先来看一下具体流程图

p14.png

我们可以对比SpringMVC的请求流程图对比来看

p16.png



我们可以看到，处理流程基本一样，有以下主要的点不同

- 处理核心
  - WebFlux--`DispatcherHandler`
  - SpringMvc--`DispatcherServlet`
- 返回值处理器
  - WebFlux--`HandlerResultHandler`
  - SpringMvc--`HandlerMethodReturnValueHandler`

- 内容协商配置器
  - WebFlux--`RequestedContentTypeResolverBuilder`
  - SpringMvc--`ContentNegotiationConfigurer`

还有很多就不一一例举了，想知道核心组件对比结果的同学，可以看下图。注意很多图上的组件名称相同，但是包的位置是不同的，所以大家要注意区分，不要弄混了。



### Web MVC VS. WebFlux 核心组件对比

p17.png



## 函数式端点请求处理流程

### 流程图

按照惯例，上流程图

p15.png



通过上图，我们可以看到，这个处理跟之前的注解驱动请求大有不同，但是请求的流程是万变不离其宗，只是组件有所变化。



### 源码解读

 接下来我们就跟着流程图一步一步的来解读WebFlux函数端点式请求的源码。

#### 装配阶段

由上图我们可以看到 `RouterFunctionMapping`是由`WebFluxConfigurationSupport`创建的，在之前的笔记中我们已经有说明。

我们接下来看一下`RouterFunctions`是怎么合并`RouterFunction`的并且如何关联到`RouterFunctionMapping`的。

我们先来看一下 `RouterFunctionMapping`关于装配阶段的代码。

```java
public class RouterFunctionMapping extends AbstractHandlerMapping implements InitializingBean {

   @Nullable
   private RouterFunction<?> routerFunction;
    //省略部分代码
    
    //afterPropertiesSet()方法 是组件初始化后回调 必须实现InitializingBean接口
    //
   @Override
   public void afterPropertiesSet() throws Exception {
      if (CollectionUtils.isEmpty(this.messageReaders)) {
         ServerCodecConfigurer codecConfigurer = ServerCodecConfigurer.create();
         this.messageReaders = codecConfigurer.getReaders();
      }

      //初始化routerFunction
      if (this.routerFunction == null) {
         initRouterFunctions();
      }
   }

   /**
    * Initialized the router functions by detecting them in the application context.
    * 从应用上下文中查找他们并初始化路由方法
    */
   protected void initRouterFunctions() {
      if (logger.isDebugEnabled()) {
         logger.debug("Looking for router functions in application context: " +
               getApplicationContext());
      }

      //查找合并所有路由方法的bean
      List<RouterFunction<?>> routerFunctions = routerFunctions();
      if (!CollectionUtils.isEmpty(routerFunctions) && logger.isInfoEnabled()) {
         routerFunctions.forEach(routerFunction -> logger.info("Mapped " + routerFunction));
      }
       
      //将一个请求中含有多个路由请求方法合并成一个方法
      this.routerFunction = routerFunctions.stream()
            .reduce(RouterFunction::andOther)
            .orElse(null);
   }

    //查找并合并所有路由方法
   private List<RouterFunction<?>> routerFunctions() {
       //声明 SortedRouterFunctionsContainer bean
      SortedRouterFunctionsContainer container = new SortedRouterFunctionsContainer();
       //自动注入到上下文中 
      obtainApplicationContext().getAutowireCapableBeanFactory().autowireBean(container);
      //返回路由
      return CollectionUtils.isEmpty(container.routerFunctions) ? Collections.emptyList() :
            container.routerFunctions;
   }
	//省略部分代码
   private static class SortedRouterFunctionsContainer {

      @Nullable
      private List<RouterFunction<?>> routerFunctions;

       //由上面的方法 自动注入bean时实现依赖查找，查找所有的 RouterFunction beans
       //并注入到 List<RouterFunction<?>> 中。这样就会得到所有实现路由方法的集合
      @Autowired(required = false)
      public void setRouterFunctions(List<RouterFunction<?>> routerFunctions) {
         this.routerFunctions = routerFunctions;
      }
   }

}
```



#### 请求阶段

请求阶段的核心代码就是 `org.springframework.web.reactive.DispatcherHandler#handle`方法,我们来看一下源码。

```java
@Override
public Mono<Void> handle(ServerWebExchange exchange) {
   if (logger.isDebugEnabled()) {
      ServerHttpRequest request = exchange.getRequest();
      logger.debug("Processing " + request.getMethodValue() + " request for [" + request.getURI() + "]");
   }
   if (this.handlerMappings == null) {
      return Mono.error(HANDLER_NOT_FOUND_EXCEPTION);
   }
   // 1.HTTP请求进来后执行的流程
   return Flux.fromIterable(this.handlerMappings)  //2 遍历handlerMappings定位RouterFunctionMapping
         .concatMap(mapping -> mapping.getHandler(exchange))   // 3.获取HandlerFunction
         .next()
         .switchIfEmpty(Mono.error(HANDLER_NOT_FOUND_EXCEPTION))  
         .flatMap(handler -> invokeHandler(exchange, handler))   //4.执行
         .flatMap(result -> handleResult(exchange, result));  //5. 处理结果
}
```

上面的代码已经把大部分的流程说明清楚了，那么我们来看一下lambda表达式中每个内部方法的具体实现。

首先我们来看一下步骤3的具体实现 `org.springframework.web.reactive.handler.AbstractHandlerMapping#getHandler`

```java
@Override
public Mono<Object> getHandler(ServerWebExchange exchange) {
    //调用 getHandlerInternal 方法来确定HandlerFunction
   return getHandlerInternal(exchange).map(handler -> {
      if (CorsUtils.isCorsRequest(exchange.getRequest())) {
         CorsConfiguration configA = this.globalCorsConfigSource.getCorsConfiguration(exchange);
         CorsConfiguration configB = getCorsConfiguration(handler, exchange);
         CorsConfiguration config = (configA != null ? configA.combine(configB) : configB);
         if (!getCorsProcessor().process(config, exchange) ||
               CorsUtils.isPreFlightRequest(exchange.getRequest())) {
            return REQUEST_HANDLED_HANDLER;
         }
      }
      return handler;
   });
}
```

上面一大段代码其实主要来获取handler的方法是 `getHandlerInternal(exchange)` 剩下的部分是 跨域处理的逻辑。我们看一下 这个方法。

```java
@Override
protected Mono<?> getHandlerInternal(ServerWebExchange exchange) {
   if (this.routerFunction != null) {
      ServerRequest request = ServerRequest.create(exchange, this.messageReaders);
      exchange.getAttributes().put(RouterFunctions.REQUEST_ATTRIBUTE, request);
      return this.routerFunction.route(request);  //通过路由获取到对应处理的HandlerFunction 也就是执行方法
   }
   else {
      return Mono.empty();
   }
}
```

获取到对应的`HandlerFunction`后我们就来执行第四步，调用`HandlerFunction`。

```java
private Mono<HandlerResult> invokeHandler(ServerWebExchange exchange, Object handler) {
   if (this.handlerAdapters != null) {
      for (HandlerAdapter handlerAdapter : this.handlerAdapters) {
         if (handlerAdapter.supports(handler)) {  //判断HandlerAdapters中是否支持之前获取到的handler
            return handlerAdapter.handle(exchange, handler);  //执行handler 对应下面handle的方法
         }
      }
   }
   return Mono.error(new IllegalStateException("No HandlerAdapter: " + handler));
}
```

`org.springframework.web.reactive.function.server.support.HandlerFunctionAdapter#handle`方法，这个类中的方法就是处理函数式端点请求的Adapter具体实现

```java
@Override
public Mono<HandlerResult> handle(ServerWebExchange exchange, Object handler) {
   HandlerFunction<?> handlerFunction = (HandlerFunction<?>) handler;
   ServerRequest request = exchange.getRequiredAttribute(RouterFunctions.REQUEST_ATTRIBUTE);
   return handlerFunction.handle(request)   //由lambda模式 (返回值-参数)  无需准确的方法签名
         .map(response -> new HandlerResult(handlerFunction, response, HANDLER_FUNCTION_RETURN_TYPE));   //返回HandlerResult对象 
}
```

这里的lambda模式比较难理解，我也是看了好几遍才理解马哥所说，主要是看`HandlerFunction`这个函数式接口

```java
@FunctionalInterface
public interface HandlerFunction<T extends ServerResponse> {

   /**
    * Handle the given request.
    * @param request the request to handle
    * @return the response
    */
   Mono<T> handle(ServerRequest request);

}
```

我们只需要满足 入参是`ServerRequest`类型 返回值是`  Mono<T>` 就可以执行。



调用完具体方法之后，我们就可以进行返回值解析序列化了。这里就是步骤5 处理结果。

```java
private Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
    //获取对应的返回结果处理器并处理          
   return getResultHandler(result).handleResult(exchange, result)   
       //如果出现错误或者异常 则选择对应的异常结果处理器进行处理
         .onErrorResume(ex -> result.applyExceptionHandler(ex).flatMap(exceptionResult ->                   getResultHandler(exceptionResult).handleResult(exchange, exceptionResult)));
}
```

我们再来看一下`getResultHandler`代码

```java
private HandlerResultHandler getResultHandler(HandlerResult handlerResult) {
   if (this.resultHandlers != null) {
      for (HandlerResultHandler resultHandler : this.resultHandlers) {
         if (resultHandler.supports(handlerResult)) {
            return resultHandler;
         }
      }
   }
   throw new IllegalStateException("No HandlerResultHandler for " + handlerResult.getReturnValue());
}
```

在这里我们看一下resultHandlers中都含有哪些返回值处理器

p18.png



我们通过截图可以看出返回值解析器跟流程图一一对应。



之后的8,9步就是在匹配到对应的返回值解析器之后进行返回值的封装和写会，这里要注意`DataBuffer`是NIO的写处理，最后写回到浏览器客户端。



## 总结

  其实在了解SpringMvc的请求流程源码之后，理解WebFlux就容易的多，毕竟WebFlux处理流程是模仿Servlet另起炉灶的。如果感兴趣的小伙伴可以跟着我的思路自己写一个请求方法去debugger一下加深理解。

