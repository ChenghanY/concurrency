package com.james.concurrency.propagation

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.mapper.BankAccountMapper
import com.james.concurrency.service.NestedOuterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = ConcurrencyApplication.class)
class NestedSpec extends Specification {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    NestedOuterService nestedOuterService;

    def setup() {
        bankAccountMapper.updateBalanceById(5,1L);
        bankAccountMapper.updateBalanceById(5,2L);
    }

    def "外层调用 nested 行为的事务。内层主动回滚事务，仅回滚内层逻辑"() {
        given:
        nestedOuterService.withoutTransactionThenInnerRollBack(5, 1L);

        expect:
        bankAccountMapper.selectById(1L).getBalance() == 0
    }

    def "外层有事务，内层调用 nested 行为的事务。内层主动回滚事务，虽然对同一行加锁，但是不会死锁。"() {
        when:
        nestedOuterService.lockSameRowThenInnerRollBack(5, 1L);

        then:
        // nested 声明的传播行为，从Mysql的角度来看是属于同一个事务，表现得跟required一致
        bankAccountMapper.selectById(1L).getBalance() == 0;
        bankAccountMapper.selectById(2L).getBalance() == 5;
    }

    def "外层有事务，内层调用 nested 行为的事务。内层主动回滚事务，仅回滚内层逻辑"() {
        when:
        nestedOuterService.thenInnerRollBack(5, 1L);

        then:
        bankAccountMapper.selectById(1L).getBalance() == 0;
    }

    def "外层有事务，内层调用 nested 行为的事务。外层主动回滚，内外逻辑都回滚"() {
        when:
        nestedOuterService.thenOuterRollback(5, 1L);

        then:
        bankAccountMapper.selectById(1L).getBalance() == 5;
    }

    def "外层有事务，内层调用 nested 行为的事务。外层抛Exception，内外逻辑都回滚"() {
        when:
        nestedOuterService.thenOuterRollbackWithException(5, 1L);

        then:
        thrown (RuntimeException)
        bankAccountMapper.selectById(1L).getBalance() == 5;
    }
}
