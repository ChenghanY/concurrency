package com.james.concurrency.service.impl;

import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.InnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class InnerServiceImpl implements InnerService {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeThenRollbackWithException(Integer cost, Long id) {
        bankAccountMapper.atomicUpdateBalanceByCostAndId(cost, id);
        throw new RuntimeException();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void requiredConsume(Integer cost, Long id) {
        bankAccountMapper.atomicUpdateBalanceByCostAndId(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void requiredConsumeThenRollback(Integer cost, Long id) {
        bankAccountMapper.atomicUpdateBalanceByCostAndId(cost, id);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void requiresNewConsumeThenRollback(Integer cost, Long id) {
        bankAccountMapper.atomicUpdateBalanceByCostAndId(cost, id);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void requiresNewConsume(Integer cost, Long id) {
        bankAccountMapper.atomicUpdateBalanceByCostAndId(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void nestedConsumeThenRollback(Integer cost, Long id) {
        bankAccountMapper.atomicUpdateBalanceByCostAndId(cost, id);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void nestedConsume(Integer cost, Long id) {
        bankAccountMapper.atomicUpdateBalanceByCostAndId(cost, id);
    }
}
