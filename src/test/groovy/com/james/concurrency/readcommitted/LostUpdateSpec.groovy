package com.james.concurrency.readcommitted

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.mapper.BankAccountMapper
import com.james.concurrency.service.BankAccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification


@SpringBootTest(classes = ConcurrencyApplication.class)
class LostUpdateSpec extends Specification{

    @Autowired
    BankAccountService bankAccountService;

    @Autowired
    BankAccountMapper bankAccountMapper;

    def "read committed 隔离级别下产生更新丢失"() {

    }
}
