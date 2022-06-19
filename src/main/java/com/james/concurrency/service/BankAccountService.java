package com.james.concurrency.service;

import org.springframework.transaction.annotation.Transactional;

public interface BankAccountService {

    /**
     * 用户消费
     * @param id 用户id
     * @param cost 支出金额
     */
    void consume(Long id, int cost);

    /**
     * 用户消费 - 原子操作
     * @param id 用户id
     * @param cost 支出金额
     */
    void atomicConsume(Long id, Integer cost);

    /**
     * 用户消费 - 使用for update显式加锁
     * @param id 用户id
     * @param cost 支出金额
     */
    void forUpdateConsume(Long id, Integer cost);

    /**
     * 用户消费 - 使用for update显式加锁
     * @param id 用户id
     * @param cost 支出金额
     */
    void lockInShareModeConsume(Long id, Integer cost);
}
