package com.james.concurrency.service.impl;

import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.InnerService;
import com.james.concurrency.service.RequiredOuterService;
import com.james.concurrency.service.TransactionalTestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class RequiredOuterServiceImpl implements RequiredOuterService {

    @Autowired
    BankAccountMapper mapper;

    @Autowired
    InnerService innerService;

    @Override
    public void withoutTransactionThenInnerRollBack(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiredConsumeThenRollback(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenInnerRollBack(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiredConsumeThenRollback(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenOuterRollback(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiredConsume(cost,id);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenOuterRollbackWithException(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiredConsume(cost,id);
        throw new TransactionalTestException();
    }
}
