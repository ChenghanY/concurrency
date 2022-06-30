package com.james.concurrency.multithreading.collection

import spock.lang.Specification

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors

class BlockingQueueSpec extends Specification{

    def "ArrayList 1000次并发存在更新丢失"() {
        given:
        def loopCount = 1000;
        def queue = new ArrayList<String>(3);

        when:
        def threadPool = Executors.newCachedThreadPool()
        for (int i = 0; i <loopCount; i++) {
            threadPool.execute(() ->  queue.add("A"))
            threadPool.execute(() ->  queue.remove("A"))
        }
        threadPool.shutdown()
        while(! threadPool.isTerminated());

        then:
        queue.size() > 0;
    }

    def "BlockingQueue put 和 take 方法阻塞演示, 1000次并发无异常"() {
        given:
        def loopCount = 1000;
        def queue = new ArrayBlockingQueue<String>(3);

        when:
        def threadPool = Executors.newCachedThreadPool()
        for (int i = 0; i <loopCount; i++) {
            threadPool.execute(() ->  queue.put("A"))
            threadPool.execute(() ->  queue.take())
        }
        threadPool.shutdown()
        while(! threadPool.isTerminated());

        then:
        queue.size() == 0;
    }
}
