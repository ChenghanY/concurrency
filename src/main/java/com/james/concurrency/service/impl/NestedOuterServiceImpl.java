package com.james.concurrency.service.impl;

import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.InnerService;
import com.james.concurrency.service.NestedOuterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class NestedOuterServiceImpl implements NestedOuterService {

    @Autowired
    BankAccountMapper mapper;

    @Autowired
    InnerService innerService;

    @Override
    public void withoutTransactionThenInnerRollBack(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.nestedConsumeThenRollback(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockSameRowThenInnerRollBack(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.nestedConsumeThenRollback(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenInnerRollBack(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.nestedConsumeThenRollback(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenInnerRollBackWithException(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.consumeThenRollbackWithException(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenOuterRollback(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.nestedConsume(cost,id);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenOuterRollbackWithException(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.nestedConsume(cost,id);
        throw new RuntimeException();
    }
}
