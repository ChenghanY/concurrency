package com.james.concurrency.service;

/**
 * 若：
 * @Transactional          @Transactional
 * serviceA.methodA  --->  serviceA.methodB
 *
 * 声明式事务只能识别最外层的@Transactional，serviceA.methodB被视为方法调用，无法被事务保护也就无法测试事务传播行为
 * 所以使用 {@link InnerBankAccountService} 让spring管理，测试事务传播行为
 */
public interface InnerBankAccountService {

    void requiredConsume(Integer cost, Long id);

    void requiredConsumeByRollbackBySupport(Integer cost, Long id);

    void requiredConsumeByRollbackByException(Integer cost, Long id);
}
