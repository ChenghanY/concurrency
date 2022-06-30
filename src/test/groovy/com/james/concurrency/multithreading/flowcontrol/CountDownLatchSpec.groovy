package com.james.concurrency.multithreading.flowcontrol

import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class CountDownLatchSpec extends Specification{

    def "CountDownLatch 实现一等多"() {
        given:
        def loopCount = 5
        def cantAwait = true
        def latch = new CountDownLatch(loopCount)
        def threadPool = Executors.newFixedThreadPool(loopCount);
        // 资源
        def resource = new AtomicInteger(loopCount);

        when:
        for (i in 0..<loopCount) {
            threadPool.execute(() -> {
                // 确保latch.await()在countDown之前执行
                latch.countDown()
                resource.decrementAndGet()
                cantAwait = false;
            })
        }
        while (! cantAwait)
        latch.await();

        then:
        // await 阻塞等待所有countDown执行，直至为0。
        resource.get() == 0
    }

    def "CountDownLatch 实现多等一"() {
        given:
        def loopCount = 5
        def threadPool = Executors.newFixedThreadPool(loopCount);
        // 等待准备的人
        def waitingPrepare = new AtomicInteger(loopCount);
        def begin = new CountDownLatch(1);

        when:
        for (i in 0..<loopCount) {
            threadPool.execute(() -> {
                waitingPrepare.decrementAndGet()
                begin.await()
            })
        }
        // 所有人准备好了，等待出发命令，一起出发
        while (waitingPrepare.get() > 0);
        begin.countDown();

        then:
        // 所有准备的选手 await 阻塞等待 一个countDown执行。
        waitingPrepare.get() == 0
    }

    def "CountDownLatch 多等一 和 一等多 综合使用。跑步选手等裁判员发令，等待所有选手到达终点比赛结束"() {
        given:
        def loopCount = 5
        def waitingPrepare = new AtomicInteger(loopCount);
        def begin = new CountDownLatch(1);
        def end = new CountDownLatch(loopCount);
        def threadPool = Executors.newFixedThreadPool(5);

        when:
        for (i in 0..<loopCount) {
            threadPool.execute(() -> {
                def current = waitingPrepare.decrementAndGet()
                println("选手" + current + "开始准备起跑")
                begin.await()
                // 选手实力不同 冲过终点所耗时不同
                Thread.sleep((long) (Math.random() * 10000))
                end.countDown()
                println("选手" + current + "冲过终点")
            })
        }

        // 等待选手准备完毕
        Thread.sleep(2000)
        println("裁判枪响，开始比赛!")
        begin.countDown();

        end.await();
        println("所有选手冲过 比赛完成！")
        then:
        true
    }
}
