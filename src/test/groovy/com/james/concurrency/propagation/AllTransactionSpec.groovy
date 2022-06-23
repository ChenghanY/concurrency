package com.james.concurrency.propagation

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.mapper.BankAccountMapper
import com.james.concurrency.service.AllTransactionOuterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = ConcurrencyApplication.class)
class AllTransactionSpec extends Specification {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    AllTransactionOuterService outerService;

    def setup() {
        bankAccountMapper.updateBalanceById(5,1L);
        bankAccountMapper.updateBalanceById(5,2L);
    }

    def "外层有事务，内层调用 required requires_news nested 事务。内层抛Exception并且不catch，内外逻辑都回滚"() {
        when:
        outerService.thenInnerRollBackWithExceptionNotCatch(5, 1L);

        then:
        thrown (RuntimeException);
        // 若事务嵌套，内层事务回滚同时会回滚外层事务。
        bankAccountMapper.selectById(1L).getBalance() == 5;
        bankAccountMapper.selectById(2L).getBalance() == 5;
    }
}
