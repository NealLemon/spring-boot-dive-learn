package com.web.servlet.asyn;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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
