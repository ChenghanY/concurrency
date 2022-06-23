package com.james.concurrency.service.impl;

import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.AllTransactionOuterService;
import com.james.concurrency.service.InnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AllTransactionOuterServiceImpl implements AllTransactionOuterService {

    @Autowired
    BankAccountMapper mapper;

    @Autowired
    InnerService innerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenInnerRollBackWithExceptionNotCatch(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        // requires_new 同一行重复加锁会导致死锁。所以内层逻辑访问另外一行
        innerService.consumeThenRollbackWithException(cost, id + 1);
    }
}
