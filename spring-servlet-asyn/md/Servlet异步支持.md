# SpringMVC基于Servlet异步支持

  本文是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。 

  大家都知道在springboot2.0版本之后推出了 基于Reactive Programming编程模型的WebFlux技术栈与SpringMvc 并存，稍后会有单独介绍WebFlux的相关内容，在我们看来WebFlux技术栈最简单直接的解释就是异步非阻塞。但是我们也知道在Servlet 3.0 之后 也支持异步非阻塞请求，而SpringMvc就是在Servlet引擎基础上创建的。

## 概要

  那么我们就简单介绍一下在SpringMvc技术栈下的相关的3种异步请求。

- DeferredResult
- Callable
- CompletionStage/CompletableFuture 

接下来看一下项目目录

p10

### DeferredResult

#### 官方文档

  p1.png   

  官方文档的意思是 要实现`DeferredResult` 控制器可以从不同的线程异步生成返回值 - 例如，消息队列，计划任务或其他事件。

那么我就按照文档所说，来实现一个异步请求吧。

#### 具体实现

  我们就使用一个监听器来模拟消息队列,来看看是否如文档所说，实现异步操作。在定义监听器之前，我们还需要一个阻塞队列。

##### 阻塞队列--SimilarQueueHolder

```java
/**
 * 模拟消息队列
 */
@Component
public class SimilarQueueHolder {

    //创建容量为10的阻塞队列
    private BlockingQueue<DeferredResult<String>> blockingDeque = new ArrayBlockingQueue<DeferredResult<String>>(10);

    public BlockingQueue<DeferredResult<String>> getBlockingDeque() {
        return blockingDeque;
    }

    public void setBlockingDeque(BlockingQueue<DeferredResult<String>> blockingDeque) {
        this.blockingDeque = blockingDeque;
    }
}
```

我们可以看到就是一个简单的Bean里声明了一个容量为10的阻塞队列。

##### 监听器--QueueListener

```java
/**
 * 使用监听器来模拟消息队列处理
 */
@Configuration
public class QueueListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private SimilarQueueHolder similarQueueHolder;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        new Thread(()->{
            while(true) {
                try {
                    //从队列中取出DeferredResult
                    DeferredResult<String> deferredResult = similarQueueHolder.getBlockingDeque().take();
                    printlnThread("开始DeferredResult异步处理");
                    //模拟处理时间
                    TimeUnit.SECONDS.sleep(3);
                    printlnThread("结束DeferredResult异步处理");
                    //模拟处理完成赋值
                    deferredResult.setResult("Hello World from DeferredResult");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

##### Controller--DeferredResultHelloWorldController

```java
@RestController
public class DeferredResultHelloWorldController {

    @Autowired
    private SimilarQueueHolder similarQueueHolder;

    @GetMapping("/deferred/result")
    public DeferredResult<String> deferredResultHelloWolrd() {
        printlnThread("主线程--deferredResultHelloWolrd开始执行");
        //声明异步DeferredResult
        DeferredResult<String> deferredResult = new DeferredResult<>();
        //模拟放入消息队列
        similarQueueHolder.getBlockingDeque().offer(deferredResult);
        printlnThread("主线程--deferredResultHelloWolrd结束执行");
        return deferredResult;
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

##### 启动类--SpringServletAsynBootStrap

```java
@SpringBootApplication
public class SpringServletAsynBootStrap {
    public static void main(String[] args) {
        SpringApplication.run(SpringServletAsynBootStrap.class,args);
    }
}
```

因为这个启动类是通用的，在这我就给出一次。

#### 启动测试

  我们启动一下Springboot容器然后使用PostMan测试一下。

​	p2.png

​	p3.png

  我们从返回结果和控制台打印就可以看到确实实现了异步处理。

### Callable

#### 官方文档

p4.png

  通过文档我们了解到使用JDK中的`java.util.concurrent.Callable ` 就可以通过配置的`TaskExecutor`运行给定任务来获取返回值。

#### 具体实现

  通过文档解释，我们这里需要先配置一个`TaskExecutor` 支持异步处理，如果不配置，那么Springboot会启用自身默认的`SimpleAsyncTaskExecutor `来处理异步。

##### 配置类--AsynWebConfig  配置`TaskExecutor` 支持的异步处理

```java
/**
 * 异步配置类
 */
@Configuration
public class AsynWebConfig implements WebMvcConfigurer {

    //配置自定义TaskExecutor
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(60 * 1000L);
        configurer.registerCallableInterceptors(timeoutInterceptor());
        configurer.setTaskExecutor(threadPoolTaskExecutor());
    }

    //异步处理拦截
    @Bean
    public TimeoutCallableProcessingInterceptor timeoutInterceptor() {
        return new TimeoutCallableProcessingInterceptor();
    }
    //异步线程池
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(5);
        t.setMaxPoolSize(10);
        t.setThreadNamePrefix("NEAL");
        return t;
    }

}
```

##### Controller--CallableHelloWorldController

```java
/**
 * Callback Controller层
 */
@RestController
public class CallableHelloWorldController {

    @GetMapping("/callable/hello")
    public Callable<String> helloWorld() {
        printlnThread("CallableHelloWorldController---主线程开始");
        return new Callable<String>() {
            public String call() throws Exception {
                //模拟处理时间
                printlnThread("异步处理开始---Callable");
                TimeUnit.SECONDS.sleep(3);
                printlnThread("异步处理结束---Callable");
                return "Hello World from Callable";
            }
        };

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

#### 启动测试

 启动容器，我们使用postMan请求测试。

p5.png

p6.png

  我们从返回结果和控制台打印看出了`Callable`异步处理也是可行的。

### CompletionStage/CompletableFuture 

#### 官方文档

p7.png

  我们可以看到CompletableFuture /CompletionStage  是DeferredResult  的替代方案。

#### 具体实现

##### Controller--CompletableAsynController

```java
/**
 * CompletionStage /CompletableFuture Controller层
 */
@RestController
public class CompletableAsynController {

    @GetMapping("/completion-stage")
    public CompletionStage<String> completionStage(){

        printlnThread("OtherAsynController---主线程开始");

        return CompletableFuture.supplyAsync(()->{
            //模拟处理时间
            printlnThread("异步处理开始---CompletableFuture");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            printlnThread("异步处理结束---CompletableFuture");
            return "Hello World from OtherAsynController"; // 异步执行结果
        });
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

#### 启动测试

启动容器，我们使用postMan请求测试。

p8.png

p9.png

 我们看到了线程切换的状态，证明异步也实现了。

## 总结

  SpringMVC异步处理大部分已经介绍完了，其实SpringMvc支持的异步已经能够满足我们的基本开发需要，那么为什么Spring还要引入 WebFlux 技术栈，用小马哥课中提到的就是 一种趋势，并发编程模型已经成趋势。之后会有单独介绍这方面的内容。

[DEMO地址](https://github.com/NealLemon/spring-boot-dive-learn/tree/master/spring-servlet-asyn)



