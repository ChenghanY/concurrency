package com.james.concurrency.multithreading.atomic

import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder

class AtomicSpec extends Specification{

    def "原始类型并发递增，造成更新丢失" () {
        given:
        var result = 0;
        var count = 1000;

        when:
        var executor = Executors.newFixedThreadPool(count);
        for (int j = 0; j < count; j++) {
            executor.execute(() -> {
                result++;
            })
        }
        executor.shutdown();
        while (! executor.isTerminated());

        then:
        result < count;
    }

    def "使用atomic包装的计数器，防止更新丢失" () {
        given:
        var result = new AtomicInteger(0);
        var loopCount = 1000;

        when:
        var executor = Executors.newFixedThreadPool(loopCount);
        for (int j = 0; j < loopCount; j++) {
            executor.execute(() -> {
                result.getAndAdd(1);
            })
        }
        executor.shutdown();
        while (! executor.isTerminated());

        then:
        result.get() == loopCount;
    }

    def "高并发下 使用LongAdder 性能更高。原理是竞争激烈时，使用工作线程独自自增，再merge进主内存" () {
        given:
        var result = new LongAdder();
        var count = 1000;

        when:
        var executor = Executors.newFixedThreadPool(count);
        for (int j = 0; j < count; j++) {
            executor.execute(() -> {
                result.increment();
            })
        }
        executor.shutdown();
        while (! executor.isTerminated());

        then:
        result.sum() == count;
    }
}
