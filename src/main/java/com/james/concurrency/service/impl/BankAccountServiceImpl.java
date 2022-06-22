package com.james.concurrency.service.impl;

import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.BankAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    @Autowired
    BankAccountMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consume(Integer cost, Long id) {
        BankAccount bankAccount = mapper.selectById(id);
        int newBalance = bankAccount.getBalance() - cost;
        mapper.updateBalanceById(newBalance, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void atomicConsume(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forUpdateConsume(Integer cost, Long id) {
        // 读操作加互斥锁
        BankAccount bankAccount = mapper.selectByIdForUpdate(id);
        int newBalance = bankAccount.getBalance() - cost;
        mapper.updateBalanceById(newBalance, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockInShareModeConsume(Integer cost, Long id) {
        // 读操作加共享锁
        BankAccount bankAccount = mapper.selectByIdLockInShareMode(id);
        int newBalance = bankAccount.getBalance() - cost;
        mapper.updateBalanceById(newBalance, id);
    }


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void OuterConsumeWithRequired(Integer balance, Long id) {
        propagationRequiredInnerConsume(balance, id);
        mapper.atomicUpdateBalanceByCostAndId(balance, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void propagationRequiredInnerConsume(Integer cost, Long id) {
        mapper.atomicUpdateBalanceByCostAndId(cost, id);
    }

}
