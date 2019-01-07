# Reactive概念的简单记录

本文是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。 

## 两种模式

- Reactive Programming：同步或异步非阻塞执行，数据传播被动通知
  - 编程模型
    - 响应式编程
    - 函数式编程
  - 设计模式
    - 观察者模式（Observer pattern ）
    - 响应流模式（Reactive streams pattern ）
    - 迭代器模式（Iterator pattern）
  - 技术栈
    - Java 8 Stream
    - Java Observable / Observer
    -  Exectuor 、 Future 、 Runnable
    - Java Iterator
  - 模式
    - 推模式（push-based）
- Imperative programming：同步阻塞执行，数据主动获取
  - 模式
    - 拉模式（pull-based）



Reactive Programming 作为观察者模式（Observer） 的延伸，在处理流式数据的过程中，并非使用传统
的命令编程方式（ Imperative programming）同步拉取数据，如迭代器模式（Iterator） ，而是采用同步
或异步非阻塞地推拉相结合的方式，响应数据传播时的变化。



## Reactive Programming 使用场景

- Reactive Streams Specification for the JVM 
  - 管理流式数据交换（ govern the exchange of stream data）
  - 异步边界（asynchronous boundary）
- Spring Framework
  - 通常并非让应用运行更快速（generally do not make applications run faster）
  - 利用较少的资源提升伸缩性（scale with a small, fixed number of threads and less memory）

- 

  Reactive Programming 作为观察者模式（Observer） 的延伸，不同于传统的命令编程方式（ Imperative
programming）同步拉取数据的方式，如迭代器模式（Iterator） 。而是采用数据发布者同步或异步地推
送到数据流（Data Streams）的方案。当该数据流（Data Steams）订阅者监听到传播变化时，立即作出
响应动作。在实现层面上，Reactive Programming 可结合函数式编程简化面向对象语言语法的臃肿性，
屏蔽并发实现的复杂细节，提供数据流的有序操作，从而达到提升代码的可读性，以及减少 Bugs 出现的
目的。同时，Reactive Programming 结合背压（Backpressure）的技术解决发布端生成数据的速率高于
订阅端消费的问题。



## Reactive Streams 规范

Reactive Streams is a standard and specification for Stream-oriented libraries for the JVM that

- process a potentially unbounded number of elements
- in sequence,
- asynchronously passing elements between components,
- with mandatory non-blocking backpressure.

### API 组件

- Publisher(数据发布者，数据上游)

  - 接口

    - ```java
      public interface Publisher<T> {
      public void subscribe(Subscriber<? super T> s);
      }
      ```

      

- Subscriber(数据订阅者，数据上游)

  - 接口

    - ```java
      public interface Subscriber<T> {
      public void onSubscribe(Subscription s);  //当下游订阅时
      public void onNext(T t);  //当下游接收数据时
      public void onError(Throwable t);  //当数据流（Data Streams）执行完成时
      public void onComplete();  //当数据流（Data Streams）执行错误时
      }
      ```

- Subscription(订阅信号控制) ----背压控制

  - 接口

    - ```java
      public interface Subscription {
      public void request(long n);  //请求上游元素的数量
      public void cancel();  //请求停止发送数据并且清除资源
      }
      ```

- Processor(消息发布者和订阅者综合体)

  - 接口

    - ```java
      public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
      }
      ```



### 背压（Backpressure）

​	假设下游Subscriber工作在无边界大小的数据流水线时，当上游Publisher提供数据的速率快于下游
Subscriber的消费数据速率时，下游Subscriber将通过传播信号（request）到上游Publisher，请求限制
数据的数量（ Demand ）或通知上游停止数据生产。





## 总结

​	这里只是Reactive相关知识介绍，觉得想深入的了解一下 需要理解Netty等异步框架。以后有机会会研究研究。



