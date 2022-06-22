package com.james.concurrency.service.impl;

import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.InnerBankAccountService;
import com.james.concurrency.service.RequiredOuterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class RequiredOuterServiceImpl implements RequiredOuterService {

    @Autowired
    BankAccountMapper mapper;

    @Autowired
    InnerBankAccountService innerService;

    @Override
    public void emptyInvokeInner(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiredConsumeByRollbackBySupport(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transactionalInvokeInnerRollBackBySupport(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiredConsumeByRollbackBySupport(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transactionalInvokeInnerRollBackByException(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiredConsumeByRollbackByException(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outerRollbackWithSupportInvokeInner(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiredConsume(cost,id);
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outerRollbackWithExceptionInvokeInner(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
        innerService.requiredConsume(cost,id);
        throw new RuntimeException();
    }
}
