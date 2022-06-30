package com.james.concurrency.multithreading.collection

import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

class WrongWayDoThreadSafeCollectionSpec extends Specification{

    def "CopyOnWriteArrayList 数据过期问题"() {
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

        /*
           源码中get是非阻塞读，获取最新的数据而不是快照的数据，快照数据比最新的少一个，get会索引越界
            public E get(int index) {
                return get(getArray(), index);
            }
         */
        for (int j = 0; j < loopCount; j++) {
            copyOnWriteList.get(j);
        }

        then:
        // 上述的索引访问会抛异常
        thrown(ArrayIndexOutOfBoundsException)
        // 迭代器访问旧版本，可行
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

}
