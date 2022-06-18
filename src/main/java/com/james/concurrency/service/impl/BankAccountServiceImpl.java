package com.james.concurrency.service.impl;

import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.BankAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    @Autowired
    BankAccountMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consume(Long id, int cost) {
        BankAccount bankAccount = mapper.selectById(id);
        int newBalance = bankAccount.getBalance() - cost;
        mapper.updateBalanceById(newBalance, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void atomicConsume(Long id, Integer cost) {
        mapper.increaseBalanceById(cost, id);
    }
}
