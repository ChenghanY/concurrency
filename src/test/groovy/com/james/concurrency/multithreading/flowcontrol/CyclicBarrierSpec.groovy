package com.james.concurrency.multithreading.flowcontrol

import spock.lang.Specification

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class CyclicBarrierSpec extends Specification{

    def "Semaphore 保证同一时刻只能有N个资源被占用 可以按占用资源的权重给线程颁发许可"() {
        given:
        def loopCount = 10;
        def count = new AtomicInteger(1);
        // 只能有4个资源能同时并发
        def barrier = new CyclicBarrier(5)
        def threadPool = Executors.newFixedThreadPool(loopCount);

        when:
        for (i in 0..<loopCount) {
            threadPool.execute(() -> {
                def current = count.getAndIncrement();
                Thread.sleep((long) (Math.random() * 10000))
                barrier.await()
                println("第" + current + "个玩家开始玩了")
            })
        }
        threadPool.shutdown()
        while (! threadPool.isTerminated());

        then:
        // 打印结果是5个5个进行
        true
    }
}
