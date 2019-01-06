package com.web.servlet.asyn;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

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
