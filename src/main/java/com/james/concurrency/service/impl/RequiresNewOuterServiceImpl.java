package com.james.concurrency.service.impl;

import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.InnerService;
import com.james.concurrency.service.RequiresNewOuterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 *  RequiresNew 隔离下若对同一行加锁会产生死锁。{@link this#lockSameRowThenInnerRollBack 会产生死锁的调用}
 *  除了上述方法，锁都会加到不同行，突出事务隔离的意义。
 */
@Service
public class RequiresNewOuterServiceImpl implements RequiresNewOuterService {

    @Autowired
    BankAccountMapper mapper;

    @Autowired
    InnerService innerService;

    @Override
    public void withoutTransactionThenInnerRollBack(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiresNewConsumeThenRollback(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockSameRowThenInnerRollBack(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiresNewConsumeThenRollback(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenInnerRollBack(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiresNewConsumeThenRollback(cost, id + 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenOuterRollback(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiresNewConsume(cost,id + 1);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thenOuterRollbackWithException(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiresNewConsume(cost,id + 1);
        throw new RuntimeException();
    }
}
