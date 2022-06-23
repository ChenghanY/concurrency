package com.james.concurrency.multithreading

import com.james.concurrency.ConcurrencyApplication
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = ConcurrencyApplication.class)
class CustomerSpec extends Specification{

    def "初始化" () {
        expect:
        1 == 1
    }
}
