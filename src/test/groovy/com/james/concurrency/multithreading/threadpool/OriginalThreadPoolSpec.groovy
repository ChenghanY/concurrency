package com.james.concurrency.multithreading.threadpool

import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

class OriginalThreadPoolSpec extends Specification{

    def "请使用原始构造方法创建线程池, 避免发生oom"() {
        def pool = Executors.newFixedThreadPool(5)
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10,);
    }
}
