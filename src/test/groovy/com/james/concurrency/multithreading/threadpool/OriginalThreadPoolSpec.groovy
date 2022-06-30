package com.james.concurrency.multithreading.threadpool

import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import static java.util.concurrent.TimeUnit.NANOSECONDS

class OriginalThreadPoolSpec extends Specification{

    def "观察工具类生成的线程池，会出现无界队列或无界最大线程数，请使用原始构造方法创建线程池, 避免发生oom"() {
        given:
        def fixedThreadPool = Executors.newFixedThreadPool(5)
        // FixedThreadPool 的等价参数
        fixedThreadPool = new ThreadPoolExecutor(
                5, 5,
                0L, TimeUnit.MILLISECONDS, // keepAliveTime 表示突破 corePoolSize 后加入的线程多久回收
                new LinkedBlockingQueue<Runnable>())

        def cachedThreadPool = Executors.newCachedThreadPool()
        cachedThreadPool = new ThreadPoolExecutor(0,    // 无核心线程数，会回收所有空闲线程
                Integer.MAX_VALUE,                  // Cached最大线程数特别大，近乎可以认为是无界的
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>()); // 阻塞队列直接交换的，不存储元素

        def singleThreadPool = Executors.newSingleThreadExecutor()
        singleThreadPool = new ThreadPoolExecutor(1, 1,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>());

        def scheduleThreadPool = Executors.newScheduledThreadPool(5)
        scheduleThreadPool = new ThreadPoolExecutor(
                5, Integer.MAX_VALUE,
                0, NANOSECONDS,
                new ScheduledThreadPoolExecutor.DelayedWorkQueue())

        expect:
        true
    }
}
