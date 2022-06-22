package com.james.concurrency.service.impl;

import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.InnerBankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class InnerBankAccountServiceImpl implements InnerBankAccountService {

    @Autowired
    BankAccountMapper bankAccountMapper;

    /**
     * required Spring 默认的事务传播级别
     * @see this#requiredConsume
     * @see this#requiredConsumeByRollbackBySupport
     * @see this#requiredConsumeByRollbackByException
     */
    @Override
    public void requiredConsume(Integer cost, Long id) {
        bankAccountMapper.atomicUpdateBalanceByCostAndId(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void requiredConsumeByRollbackBySupport(Integer cost, Long id) {
        bankAccountMapper.atomicUpdateBalanceByCostAndId(cost, id);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void requiredConsumeByRollbackByException(Integer cost, Long id) {
        bankAccountMapper.atomicUpdateBalanceByCostAndId(cost, id);
        throw new RuntimeException();
    }
}
