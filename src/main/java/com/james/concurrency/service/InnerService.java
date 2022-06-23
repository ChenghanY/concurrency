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
    /**
     * required/requires_new/nested 调用后抛异常
     */
    void consumeThenRollbackWithException(Integer cost, Long id);

    /**
     * required
     */
    void requiredConsumeThenRollback(Integer cost, Long id);

    void requiredConsume(Integer cost, Long id);

    /**
     * requires_new
     */
    void requiresNewConsumeThenRollback(Integer cost, Long id);

    void requiresNewConsume(Integer cost, Long id);

    /**
     * nested
     */
    void nestedConsumeThenRollback(Integer cost, Long id);

    void nestedConsume(Integer cost, Long id);
}
