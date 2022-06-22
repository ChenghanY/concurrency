package com.james.concurrency.propagation

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.mapper.BankAccountMapper

import com.james.concurrency.service.RequiredOuterBankAccountService
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
 * 声明为Required的事务被调用：
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
    RequiredOuterBankAccountService bankAccountService;

    def "外层调用Required (默认) 行为的事务。内层主动回滚事务，仅回滚内层逻辑"() {
        given:
        bankAccountMapper.updateBalanceById(5,1L);
        bankAccountService.emptyInvokeInner(5, 1L);

        expect:
        bankAccountMapper.selectById(1L).getBalance() == 0
    }

    def "外层有事务，内层调用Required (默认) 行为的事务。内层主动回滚事务，外层抛出UnexpectedRollbackException异常"() {
        when:
        bankAccountMapper.updateBalanceById(5,1L);
        bankAccountService.transactionalInvokeInnerRollBackBySupport(5, 1L);

        then:
        thrown (UnexpectedRollbackException)
    }

    def "外层有事务，内层调用Required (默认) 行为的事务。内层抛Exception，内外逻辑都回滚"() {
        when:
        bankAccountMapper.updateBalanceById(5,1L);
        bankAccountService.transactionalInvokeInnerRollBackByException(5, 1L);

        then:
        thrown (RuntimeException);
        // 若事务嵌套，内层事务回滚同时会回滚外层事务。
        bankAccountMapper.selectById(1L).getBalance() == 5;
    }

    def "外层有事务，内层调用Required (默认) 行为的事务。外层主动回滚，内外逻辑都回滚"() {
        when:
        bankAccountMapper.updateBalanceById(5,1L);
        bankAccountService.outerRollbackWithSupportInvokeInner(5, 1L);

        then:
        bankAccountMapper.selectById(1L).getBalance() == 5;
    }

    def "外层有事务，内层调用Required (默认) 行为的事务。外层抛Exception，内外逻辑都回滚"() {
        when:
        bankAccountMapper.updateBalanceById(5,1L);
        bankAccountService.outerRollbackWithExceptionInvokeInner(5, 1L);

        then:
        thrown (RuntimeException)
        bankAccountMapper.selectById(1L).getBalance() == 5;
    }

}
