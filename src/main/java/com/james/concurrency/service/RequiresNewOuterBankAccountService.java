package com.james.concurrency.service;

public interface RequiresNewOuterBankAccountService {

    /**
     * 传播行为Required测试 。Spring默认
     */
    void emptyInvokeInner(Integer balance, Long id);

    void transactionalInvokeInnerRollBackBySupport(Integer cost, Long id);

    void transactionalInvokeInnerRollBackByException(Integer cost, Long id);

    void outerRollbackWithSupportInvokeInner(Integer cost, Long id);

    void outerRollbackWithExceptionInvokeInner(Integer cost, Long id);
}
