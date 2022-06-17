package com.james.concurrency.service.impl;

import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    int count = 0;

    @Autowired
    BankAccountMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consume(Long id, int cost) throws InterruptedException {
        BankAccount bankAccount = mapper.selectById(id);
        int newBalance = bankAccount.getBalance() - cost;
        mapper.updateBalanceById(newBalance, id);
        if (count == 0) {
            count++;
            Thread.sleep(5000);
            throw new RuntimeException();
        }
    }
}
