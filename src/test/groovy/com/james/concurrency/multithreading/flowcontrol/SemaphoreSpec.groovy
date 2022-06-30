package com.james.concurrency.multithreading.flowcontrol

import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

class SemaphoreSpec extends Specification{

    def "Semaphore 保证同一时刻只能有N个资源被占用 可以按占用资源的权重给线程颁发许可"() {
        given:
        def loopCount = 6;
        def count = new AtomicInteger(1);
        // 只能有4个资源能同时并发
        def semaphore = new Semaphore(4, true)
        def threadPool = Executors.newFixedThreadPool(loopCount);

        when:
        for (i in 0..<loopCount) {
            threadPool.execute(() -> {
                semaphore.acquire(2)
                // 避免死锁，拿多少个资源就放多少个
                Thread.sleep((long) (Math.random() * 10000))
                println("第" + count.getAndIncrement() + "个线程开始处理")
                semaphore.release(2)
            })
        }
        threadPool.shutdown()
        while (! threadPool.isTerminated());

        then:
        true
    }
}
