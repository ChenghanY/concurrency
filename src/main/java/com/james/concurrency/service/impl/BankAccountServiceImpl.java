package com.james.concurrency.service.impl;

import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    @Autowired
    BankAccountMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consume(Long id, int cost) {
        BankAccount bankAccount = mapper.selectById(id);
        mapper.updateBalanceById(bankAccount.getBalance() - cost, id);
        // 验证事务生效
        throw new RuntimeException();
    }
}
