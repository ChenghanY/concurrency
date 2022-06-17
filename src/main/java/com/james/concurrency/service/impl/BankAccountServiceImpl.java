package com.james.concurrency.service.impl;

import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.BankAccountService;
import com.james.concurrency.util.TransactionalCallBack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Objects;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    @Autowired
    BankAccountMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consume(Long id, int cost, TransactionalCallBack callBack) {
        BankAccount bankAccount = mapper.selectById(id);
        int newBalance = bankAccount.getBalance() - cost;
        mapper.updateBalanceById(newBalance, id);
        if (Objects.nonNull(callBack)) {
            callBack.doSomethingBeforeCommit();
        }
    }
}
