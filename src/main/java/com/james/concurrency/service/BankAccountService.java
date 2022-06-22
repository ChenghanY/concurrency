package com.james.concurrency.service;

public interface BankAccountService {

    /**
     * 用户消费
     * @param id 用户id
     * @param cost 支出金额
     */
    void consume(Integer cost, Long id);

    /**
     * 用户消费 - 原子操作
     * @param id 用户id
     * @param cost 支出金额
     */
    void atomicConsume(Integer cost, Long id);

    /**
     * 用户消费 - 使用for update显式加锁
     * @param id 用户id
     * @param cost 支出金额
     */
    void forUpdateConsume(Integer cost, Long id);

    /**
     * 用户消费 - 使用for update显式加锁
     * @param id 用户id
     * @param cost 支出金额
     */
    void lockInShareModeConsume(Integer cost, Long id);


    /**
     * 传播行为Required测试 。Spring默认
     */
    void propagationRequiredInnerConsume(Integer balance, Long id);
    void OuterConsumeWithRequired(Integer balance, Long id);
}
