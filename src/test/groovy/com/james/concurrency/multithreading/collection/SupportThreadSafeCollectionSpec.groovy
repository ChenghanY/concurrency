package com.james.concurrency.multithreading.collection

import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

/**
 * 支持线程安全的集合使用。“支持” 表达线程安全与否是交给程序员自己掌握，错误的使用同样会造成不安全的情况。
 * {@link com.james.concurrency.multithreading.collection.WrongWayDoThreadSafeCollectionSpec 错误使用的用例}
 */
class SupportThreadSafeCollectionSpec extends Specification{

    def "CopyOnWriteArrayList 用迭代器先获取快照。仅写加锁，且写写互斥，适合读多写少的情况" () {
        given:
        def loopCount = 10;
        def copyOnWriteList = new CopyOnWriteArrayList<Integer>()
        for (int j = 0; j < loopCount; j++) {
            copyOnWriteList.add(1);
        }

        when:
        /*
            用迭代器先获取快照，源码中获取的是一个快照。支持可重复读。但是同时更改不会在这个视图生效。
            private COWIterator(Object[] elements, int initialCursor) {
                cursor = initialCursor;
                snapshot = elements;
            }
         */
        def iterator = copyOnWriteList.iterator()
        def otherThread = new Thread(() -> copyOnWriteList.remove(3))
        otherThread.start()
        otherThread.join()

        then:
        // 迭代器访问旧版本
        iterator.sum() == 10
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
