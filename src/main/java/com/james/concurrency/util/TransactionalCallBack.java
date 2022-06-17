package com.james.concurrency.util;

@FunctionalInterface
public interface TransactionalCallBack {

    void doSomethingBeforeCommit();
}
