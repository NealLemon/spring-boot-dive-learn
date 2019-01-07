# Springboot 2.0---WebFlux

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
    - 用于函数式接口类型声明的信息注解类型，这些接口的实例被 Lambda 表示式、方法引用或构造器引用创建。函
      数式接口只能有一个抽象方法，并排除接口默认方法以及声明中覆盖 Object 的公开方法的统计。同时，@FunctionalInterface 不能标注在注解、类以及枚举上。如果违背以上规则，那么接口不能视为函数式接口，当标注 @FunctionalInterface 后，会引起编译错误。不过，如果任一接口满足以上函数式接口的要求，无论接口声明中是否标注 @FunctionalInterface ，均能被编译器视作函数式接口。
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

#### 官方文档



