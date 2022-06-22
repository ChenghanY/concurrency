package com.james.concurrency.service.impl;

import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.InnerService;
import com.james.concurrency.service.RequiresNewOuterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class RequiresNewOuterServiceImpl implements RequiresNewOuterService {

    @Autowired
    BankAccountMapper mapper;

    @Autowired
    InnerService innerService;

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
