package com.james.concurrency.service;

public interface RequiredOuterService {

    /**
     * 传播行为Required测试 。Spring默认
     */
    void withoutTransactionThenInnerRollBack(Integer balance, Long id);

    void thenInnerRollBack(Integer cost, Long id);

    void thenInnerRollBackWithException(Integer cost, Long id);

    void thenOuterRollback(Integer cost, Long id);

    void thenOuterRollbackWithException(Integer cost, Long id);
}
