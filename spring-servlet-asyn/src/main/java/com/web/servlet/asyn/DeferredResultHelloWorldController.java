package com.web.servlet.asyn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

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
