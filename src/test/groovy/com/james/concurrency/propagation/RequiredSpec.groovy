package com.james.concurrency.propagation

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.mapper.BankAccountMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = ConcurrencyApplication.class)
class RequiredSpec extends Specification {

    @Autowired
    BankAccountMapper bankAccountMapper;


}
