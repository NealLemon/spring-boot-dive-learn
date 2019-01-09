# Springboot 2.0---WebFlux初识

  本文是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。 

## 简介

  由于Spring5.0(Springboot 2.0)之后，官方引入了全新的技术栈，对于开发者而言，Spring总会给我们带来惊喜，但是通过之前Reactive一篇文章，我们也知道，这种技术栈并不是新技术，而是Spring将之前的已存在的编程模型嵌入到了Spring中。

### 基本介绍

  Spring WebFlux 是一套全新的 Reactive Web 栈技术，实现完全非阻塞，支持 Reactive Streams 背压等特性，并且运行环境不限于 Servlet 容器（Tomcat、Jetty、Undertow），如 Netty 等。Spring WebFlux 与 Spring MVC 可共存，在 Spring Boot 中，Spring MVC 优先级更高。

### 实际动机

  从 Spring MVC 注解驱动的时代开始，Spring 官方有意识地去 Servlet 化。不过在 Spring MVC 的时代，Spring
扔拜托不了 Servlet 容器的依赖，然而 Spring 借助 Reactive Programming 的势头，WebFlux 将 Servlet 容器从必须项变为可选项，并且默认采用 Netty Web Server 作为基础，从而组件地形成 Spring 全新技术体系，包括数据存储等技术栈。

## API组件以及编程模型

#### API组件

- Mono
  - 0-1 的非阻塞结果
  - Reactive Streams JVM API Publisher
  - 非阻塞 Optional
- Flux
  - 0-N 的非阻塞序列
  - Reactive Streams JVM API Publisher
  - 非阻塞 Stream

#### 编程模型

- 注解驱动 （Annotated Controllers）
  - 大多与SpringMVC注解一致
  - Spring MVC 和 Spring WebFlux 均能使用注解驱动 Controller，然而不同点在于并发模型和阻塞特性。
  - Spring MVC 通常是 Servlet 应用，因此，可能被当前线程阻塞。以远程调用为例，由于阻塞的缘故，导致 Servlet容器使用较大的线程池处理请求。
- 函数式端点（Functional Endpoints）
- Spring WebFlux 通常是非阻塞服务，不会发生阻塞，因此该阻塞服务器可使用少量、固定大小的线程池处理请
  求。
  - Spring WebFlux 通常是非阻塞服务，不会发生阻塞，因此该阻塞服务器可使用少量、固定大小的线程池处理请求。
  - 函数式接口 - @FunctionInterface
    - 用于函数式接口类型声明的信息注解类型，这些接口的实例被 Lambda 表示式、方法引用或构造器引用创建。函数式接口只能有一个抽象方法，并排除接口默认方法以及声明中覆盖 Object 的公开方法的统计。同时，@FunctionalInterface 不能标注在注解、类以及枚举上。如果违背以上规则，那么接口不能视为函数式接口，当标注 @FunctionalInterface 后，会引起编译错误。不过，如果任一接口满足以上函数式接口的要求，无论接口声明中是否标注 @FunctionalInterface ，均能被编译器视作函数式接口。
    - 接口函数
      - 消费函数 - Consumer
      - 生产函数 - Supplier
      - 处理函数 - Function
      - 判定函数 - Predicate
  - 映射路由接口 - RouterFunction
    - 路由方法 - RouteFunctions#route
    - 请求判定 - RequestPredicate
    - 处理器函数 - HandlerFunction

##   简单实现

  基本内容和编程模型已经照着马哥的课纲摘取的抄下来了，接下来我们先来简单实现 Spring WebFlux Framework。接下来我们就跟着官方文档的示例来简单实现一下webflux的基本功能。

#### 注解驱动实现

##### 官方示例

p1.png



可以看出，基于注解驱动实现的webFlux Framework 与 SpringMvc没有太大差别，现在就让我们动手来实现一下吧。

##### 具体代码

1.按照官方的示例 我们需要一个 User实体类。

```java
/**
 * @ClassName User
 * @Description 用户实体类
 * @Author Neal
 * @Date 2019/1/8 9:55
 * @Version 1.0
 */
public class User {

    //用户ID
    private int userId;

    //用户姓名
    private String userName;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
```

2.简单的仓储

```java
/**
 * @ClassName UserRepository
 * @Description 用户仓储
 * @Author Neal
 * @Date 2019/1/8 11:19
 * @Version 1.0
 */
@Repository
public class UserRepository {

    //模拟数据库存储
    private static Map<Integer,User> userMap = new HashMap<>();

    //初始化仓储数据
    static {
        User user1 = new User();
        user1.setUserId(1);
        user1.setUserName("用户1");
        userMap.put(1,user1);
        User user2 = new User();
        user2.setUserId(2);
        user2.setUserName("用户2");
        userMap.put(2,user2);
    }

    public Map<Integer,User> getUserByUserId() {
        printlnThread("调用getUserByUserId");
        return userMap;
    }


    public Map<Integer,User> getUsers() {
        printlnThread("调用getUsers");
        return userMap;
    }

    /**
     * 打印当前线程
     * @param object
     */
    private void printlnThread(Object object) {
        String threadName = Thread.currentThread().getName();
        System.out.println("HelloWorldAsyncController[" + threadName + "]: " + object);
    }
}
```

3.controller层

```java
/**
 * @ClassName WebFluxAnnotatedController
 * @Description
 * @Author Neal
 * @Date 2019/1/8 10:17
 * @Version 1.0
 */
@RestController
@RequestMapping("/annotated/")
public class WebFluxAnnotatedController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 查询单个用户
     * @param id
     * @return  返回Mono 非阻塞单个结果
     */
    @GetMapping("user/{id}")
    public Mono<User> getUserByUserId(@PathVariable("id") int id) {
        return Mono.just(userRepository.getUserByUserId().get(id));
    }

    /**
     *
     * @return  返回Flux 非阻塞序列
     */
    @GetMapping("users")
    public Flux<User> getAll() {
        printlnThread("获取HTTP请求");
        //使用lambda表达式
        return Flux.fromStream(userRepository.getUsers().entrySet().stream().map(Map.Entry::getValue));
    }

    /**
     * 打印当前线程
     * @param object
     */
    private void printlnThread(Object object) {
        String threadName = Thread.currentThread().getName();
        System.out.println("HelloWorldAsyncController[" + threadName + "]: " + object);
    }
}
```





##### 启动测试

  接下来让我们启动一下容器，并且调用REST API 来测试一下返回结果是否符合预期。

 单个结果返回,也就是获取一个用户

p3.png

p4,png

获取结果序列，也就是全部用户

p5.png

p6.png



#### 函数式端点实现

##### 官方示例

p2.png

  WebFlux使用配置函数路由的方式来实现请求映射，而在处理接口(`UserHandler `) 中的方法返回全都是`Mono<ServerResponse>`类型的，这个就跟函数式接口`@FunctionInterface`有关，有兴趣的小伙伴可以仔细了解一下。这里就作简单的解释。先看这个`route`方法。

```java
public static <T extends ServerResponse> RouterFunction<T> route(
      RequestPredicate predicate, HandlerFunction<T> handlerFunction) {

   return new DefaultRouterFunction<>(predicate, handlerFunction);
}
```

 这个方法需要返回一个 ` <T extends ServerResponse>` 。

而在 `DefaultRouterFunction` 类中

```java
private static final class DefaultRouterFunction<T extends ServerResponse> extends AbstractRouterFunction<T> {

   private final RequestPredicate predicate;

   private final HandlerFunction<T> handlerFunction;

   public DefaultRouterFunction(RequestPredicate predicate, HandlerFunction<T> handlerFunction) {
      Assert.notNull(predicate, "Predicate must not be null");
      Assert.notNull(handlerFunction, "HandlerFunction must not be null");
      this.predicate = predicate;
      this.handlerFunction = handlerFunction;
   }

   @Override
   public Mono<HandlerFunction<T>> route(ServerRequest request) {
      if (this.predicate.test(request)) {
         if (logger.isDebugEnabled()) {
            logger.debug(String.format("Predicate \"%s\" matches against \"%s\"", this.predicate, request));
         }
         return Mono.just(this.handlerFunction);
      }
      else {
         return Mono.empty();
      }
   }

   @Override
   public void accept(Visitor visitor) {
      visitor.route(this.predicate, this.handlerFunction);
   }
}
```

我们可以看到 route的返回值是 `Mono<HandlerFunction<T>>` 而`Mono<HandlerFunction<T>>`就是一个函数式接口

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

所以 路由函数返回值只能是 `Mono<T extends ServerResponse>` 类型。



##### 具体代码

1.路由配置类 `WebFluxRoutingConfiguration`

```java
/**
 * @ClassName WebFluxRoutingConfiguration
 * @Description 函数式端点
 * @Author Neal
 * @Date 2019/1/8 14:28
 * @Version 1.0
 */
@Configuration
public class WebFluxRoutingConfiguration {

    @Autowired
    private UserHandler userHandler;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route(GET("/webflux/user/{userId}"), userHandler::getUserById)
                .andRoute(GET("/webflux/users"),userHandler::getAll);
    }

}
```

2.处理类`UserHandler`

```java
/**
 * @ClassName UserHandler
 * @Description TODO
 * @Author Neal
 * @Date 2019/1/8 14:30
 * @Version 1.0
 */
@Component
public class UserHandler {

    @Autowired
    private UserRepository userRepository;

    public Mono<ServerResponse> getUserById(ServerRequest serverRequest) {
        printlnThread("获取单个用户");
        return ServerResponse.status(HttpStatus.OK)
                .body(Mono.just(userRepository.getUserByUserId().get(Integer.valueOf(serverRequest.pathVariable("userId")))), User.class);
    }


    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
        printlnThread("获取所有用户");
        Flux<User> userFlux = Flux.fromStream(userRepository.getUsers().entrySet().stream().map(Map.Entry::getValue));
        return ServerResponse.ok()
                .body(userFlux, User.class);
    }
    /**
     * 打印当前线程
     * @param object
     */
    private void printlnThread(Object object) {
        String threadName = Thread.currentThread().getName();
        System.out.println("HelloWorldAsyncController[" + threadName + "]: " + object);
    }
}
```

##### 启动测试

启动Springboot使用postMan请求。

获取单个用户

p7.png

p8.png

获取所有用户

p9.png

p10.png

总结

  Spring WebFlux Framework的两种模式的简单实现已经介绍完了。大家可能会有疑问，不说是异步非阻塞么，为什么在控制台输出的线程总是单线程处理的，这好像跟异步没有关系吧。在这里要纠正一下我们理解上的错误。这里指的异步非阻塞并不是说使用增多线程来实现非阻塞，而是HTTP请求的非阻塞。

  举个简单的例子:

  之前Servlet的同步阻塞就相当于 远途大客车。而WebFlux非阻塞相当于 公交车。假设两辆车的座位数量相等。我们都知道远途的大客车只乘客不允许乘客站乘，也就是说座位数固定下只有当一个乘客下车后才可以再上一个乘客。而公交车呢，只要到站就可以上车下车，没有人员数量限制。

  不知道我的例子大家能不能看懂，例子中的座位就是我们tomcat或其他容器的线程总数，而请求就是上车的人员。我们在有限的线程中，只有WebFlux可以做到非阻塞的请求。

  但是我们要注意一点，使用WebFlux或Reactive编程模型时，一定要注意超时的问题。



## 其他链接

[Demo地址](https://github.com/NealLemon/spring-boot-dive-learn/tree/master/springboot-webflux)

[朱晔和你聊Spring系列S1E5：Spring WebFlux小探](https://zhuanlan.zhihu.com/p/46013409)

[左搜-Spring-WebFlux](http://www.leftso.com/blog/285.html)

[Springboot doc](https://docs.spring.io/spring-boot/docs/2.1.1.RELEASE/reference/htmlsingle/#boot-features-webflux)