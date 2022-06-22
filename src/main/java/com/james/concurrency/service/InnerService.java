package com.james.concurrency.service;

/**
 * 若：
 * @Transactional          @Transactional
 * serviceA.methodA  --->  serviceA.methodB
 *
 * 声明式事务只能识别最外层的@Transactional，serviceA.methodB被视为方法调用，无法被事务保护也就无法测试事务传播行为
 * 所以使用 {@link InnerService} 让spring管理，测试事务传播行为
 */
public interface InnerService {

    void requiredConsume(Integer cost, Long id);

    void requiredConsumeThenRollback(Integer cost, Long id);

    void requiredConsumeThenRollbackWithException(Integer cost, Long id);

    void requiresNewConsumeThenRollback(Integer cost, Long id);

    void requiresNewConsumeThenRollbackByException(Integer cost, Long id);

    void requiresNewConsume(Integer cost, Long id);
}
