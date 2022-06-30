package com.james.concurrency.multithreading.collection

import spock.lang.Specification

import java.util.concurrent.Executors

/**
 * 同步集合的使用
 */
class UnThreadSafeCollectionSpec extends Specification{

    def "ArrayList 使用for i 造成索引越界"() {
        given:
        def givenSize = 5;
        def list = new ArrayList<Integer>()
        for (int i = 0; i< givenSize; i++) {
            list.add(i);
        }

        when:
        for (int i = 0; i< givenSize; i++) {
            list.remove(i);
        }

        then:
        thrown(IndexOutOfBoundsException)
    }

    def "ArrayList 使用for i 造成漏删数据"() {
        given:
        def givenSize = 5;
        def list = new ArrayList<Integer>()
        for (int i = 0; i< givenSize; i++) {
            list.add(i);
        }

        when:
        for (int i = 0; i< list.size(); i++) {
            list.remove(i);
        }

        then:
        ! list.isEmpty()
    }

    def "ArrayList 使用迭代器删除，避免隐患"() {
        given:
        def givenSize = 5;
        def list = new ArrayList<Integer>()

        when:
        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            iterator.remove();
        }

        then:
        list.isEmpty()
    }

    def "ArrayList 更新丢失" () {
        given:
        def loopCount = 100;
        // arrayList共享变量
        def list = new ArrayList<Integer>(1)
        list.add(Integer.valueOf(0));

        when:
        def executor = Executors.newFixedThreadPool(loopCount);
        for (int j = 0; j < loopCount; j++) {
            executor.execute(() -> {list.set(0, list.get(0) +1);} as Runnable)
        }
        executor.shutdown();
        while (! executor.isTerminated());

        then:
        list.get(0) < 100;
    }

}
