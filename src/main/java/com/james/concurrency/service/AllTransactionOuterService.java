package com.james.concurrency.service;

public interface AllTransactionOuterService {

    void thenInnerRollBackWithExceptionNotCatch(Integer cost, Long id);
}
