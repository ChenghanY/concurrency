package com.james.concurrency.service;

import com.james.concurrency.util.TransactionalCallBack;

public interface BankAccountService {

    /**
     * 用户消费
     * @param id 用户id
     * @param cost 支出金额
     */
    void consume(Long id, int cost, TransactionalCallBack callBack);
}
