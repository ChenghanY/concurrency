package com.james.concurrency.service;

public interface NestedOuterService {

    void withoutTransactionThenInnerRollBack(Integer cost, Long id);

    void thenInnerRollBack(Integer cost, Long id);

    void lockSameRowThenInnerRollBack(Integer cost, Long id);

    void thenInnerRollBackWithException(Integer cost, Long id);

    void thenOuterRollback(Integer cost, Long id);

    void thenOuterRollbackWithException(Integer cost, Long id);
}
