package com.james.concurrency.propagation

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.mapper.BankAccountMapper
import com.james.concurrency.service.RequiredOuterService
import com.james.concurrency.service.TransactionalTestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.UnexpectedRollbackException
import spock.lang.Specification


/**
 * 官方：https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#tx-propagation
 *
 * PROPAGATION_REQUIRED enforces a physical transaction,
 * either locally for the current scope if no transaction exists yet or participating in an existing 'outer' transaction
 * defined for a larger scope. This is a fine default in common call stack arrangements within the same thread
 * (for example, a service facade that delegates to several repository methods
 * where all the underlying resources have to participate in the service-level transaction).
 *
 * REQUIRED 声明的方法必须要处于事务中。如果外层有事务则加入到外层事务（两者代码合并成一个大事务）。
 * 如果外层无事务，则为内层代码生成新事务。
 *
 *
 * 外部无事务：
 *      内部回滚不影响外部
 *
 * 外部有事务
 *      内部借助{@link org.springframework.transaction.interceptor.TransactionAspectSupport} 回滚会抛异常
 *      内部抛异常，内外逻辑都回滚
 *      外部回滚，内外逻辑都回滚
 */
@SpringBootTest(classes = ConcurrencyApplication.class)
class RequiredSpec extends Specification {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    RequiredOuterService requiredOuterService;

    def setup() {
        bankAccountMapper.updateBalanceById(5,1L);
    }

    def "外层调用 required (默认) 行为的事务。内层主动回滚事务，仅回滚内层逻辑"() {
        given:
        requiredOuterService.withoutTransactionThenInnerRollBack(5, 1L);

        expect:
        bankAccountMapper.selectById(1L).getBalance() == 0
    }

    def "外层有事务，内层调用 required (默认) 行为的事务。内层主动回滚事务，外层抛出UnexpectedRollbackException异常"() {
        when:
        requiredOuterService.thenInnerRollBack(5, 1L);

        then:
        thrown (UnexpectedRollbackException)
    }

    def "外层有事务，内层调用 required (默认) 行为的事务。外层主动回滚，内外逻辑都回滚"() {
        when:
        requiredOuterService.thenOuterRollback(5, 1L);

        then:
        bankAccountMapper.selectById(1L).getBalance() == 5;
    }

    def "外层有事务，内层调用 required (默认) 行为的事务。外层抛Exception，内外逻辑都回滚"() {
        when:
        requiredOuterService.thenOuterRollbackWithException(5, 1L);

        then:
        thrown (TransactionalTestException)
        bankAccountMapper.selectById(1L).getBalance() == 5;
    }
}
