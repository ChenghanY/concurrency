package com.james.concurrency

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import spock.lang.Specification

@SpringBootTest(classes = ConcurrencyApplication.class)
class IntegrationSpec extends Specification {

    @Autowired
    ApplicationContext context

    def "spock与spring boot集成"() {
        expect:
        context != null
        context.containsBean("concurrencyApplication")
        context.containsBean("bankAccountController")
        context.containsBean("bankAccountMapper")
        context.containsBean("bankAccountServiceImpl")
    }
}