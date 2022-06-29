package com.james.concurrency.multithreading.juc

import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

/**
 * 同步集合的使用
 */
class ThreadSafeCollectionSpec extends Specification{

    def "CopyOnWriteArrayList 用迭代器先获取快照 防止并发更改的索引越界" () {
        given:
        def loopCount = 10;
        // arrayList共享变量
        def list = new ArrayList<Integer>();
        for (int j = 0; j < loopCount; j++) {
            list.add(1);
        }
        def copyOnWrite = new CopyOnWriteArrayList<Integer>(list)

        when:
        // 用迭代器先获取快照
        def iterator = copyOnWrite.iterator()
        def executor = Executors.newFixedThreadPool(1);
        executor.execute(e -> copyOnWrite.remove(3));
        executor.shutdown();
        while (! executor.isTerminated());

        then:
        // for 循环访问下标
        for (int j = 0; j < loopCount; j++) {
            list.get(j);
        }
        // 迭代器访问
        iterator.sum() == 10
    }

    def "ConcurrentHashMap 非原子操作造成线程不安全"() {
        given:
        def expectedValue = 1000;
        def resultKey = "james";
        def loopCount = expectedValue;
        def map = new ConcurrentHashMap<String, Integer>()
        map.put(resultKey, 0);

        when:
        def executor = Executors.newFixedThreadPool(loopCount);
        for (int j = 0; j < loopCount; j++) {
            executor.execute(() -> {
                map.put("james", map.get(resultKey) + 1);
            })
        }
        executor.shutdown();
        while (! executor.isTerminated());

        then:
        expectedValue != map.get(resultKey)
    }

    def "ConcurrentHashMap 使用原子操作CAS更新避免隐患"() {
        given:
        def expectedValue = 1000;
        def resultKey = "james";
        def loopCount = expectedValue;
        def map = new ConcurrentHashMap<String, Integer>()
        map.put(resultKey, 0);

        when:
        def executor = Executors.newFixedThreadPool(loopCount);
        for (int j = 0; j < loopCount; j++) {
            executor.execute(() -> {
                while (true) {
                    def currentResult = map.get(resultKey);
                    def isSuccess = map.replace("james", currentResult,  currentResult+ 1);
                    if (isSuccess) {
                        break;
                    }
                }
            })
        }
        executor.shutdown();
        while (! executor.isTerminated());

        then:
        expectedValue == map.get(resultKey)
    }
}
