package com.james.concurrency.propagation

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.mapper.BankAccountMapper
import com.james.concurrency.service.RequiresNewOuterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.CannotAcquireLockException
import spock.lang.Specification

/**
 * 官方：https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#tx-propagation
 *
 * PROPAGATION_REQUIRES_NEW, in contrast to PROPAGATION_REQUIRED,
 * always uses an independent physical transaction for each affected transaction scope,
 * never participating in an existing transaction for an outer scope. In such an arrangement,
 * the underlying resource transactions are different and, hence, can commit or roll back independently,
 * with an outer transaction not affected by an inner transaction’s rollback status
 * and with an inner transaction’s locks released immediately after its completion.
 * Such an independent inner transaction can also declare its own isolation level, timeout,
 * and read-only settings and not inherit an outer transaction’s characteristics.
 *
 * REQUIRES_NEW 是独立于外层事务的。内层事务的方法执行完毕会立即提交或者回滚。
 *
 * note:
 *  requires_new事务传播的隐患： 外层和内层同时锁定一个行会造成死锁。
 */
@SpringBootTest(classes = ConcurrencyApplication.class)
class RequiresNewSpec extends Specification {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    RequiresNewOuterService requiresNewOuterService;

    def setup() {
        bankAccountMapper.updateBalanceById(5,1L);
        bankAccountMapper.updateBalanceById(5,2L);
    }

    def "外层调用 requires_new 行为的事务。内层主动回滚事务，仅回滚内层逻辑"() {
        given:
        requiresNewOuterService.withoutTransactionThenInnerRollBack(5, 1L);

        expect:
        bankAccountMapper.selectById(1L).getBalance() == 0
    }

    def "外层有事务，内层调用 requires_new 行为的事务。由于执行内外层对同样的行加锁，死锁发生"() {
        when:
        requiresNewOuterService.lockSameRowThenInnerRollBack(5, 1L);

        then:
        thrown (CannotAcquireLockException)
    }

    def "外层有事务，内层调用 requires_new 行为的事务。内层主动回滚事务，仅回滚内层逻辑"() {
        when:
        requiresNewOuterService.thenInnerRollBack(5, 1L);

        then:
        bankAccountMapper.selectById(1L).getBalance() == 0;
        bankAccountMapper.selectById(2L).getBalance() == 5;
    }

    def "外层有事务，内层调用 requires_new  行为的事务。外层主动回滚，仅回滚外层逻辑"() {
        when:
        requiresNewOuterService.thenOuterRollback(5, 1L);

        then:
        bankAccountMapper.selectById(1L).getBalance() == 5;
        bankAccountMapper.selectById(2L).getBalance() == 0;
    }

    def "外层有事务，内层调用 requires_new  行为的事务。外层抛Exception，仅回滚外层逻辑"() {
        when:
        requiresNewOuterService.thenOuterRollbackWithException(5, 1L);

        then:
        thrown (RuntimeException)
        bankAccountMapper.selectById(1L).getBalance() == 5;
        bankAccountMapper.selectById(2L).getBalance() == 0;
    }
}
