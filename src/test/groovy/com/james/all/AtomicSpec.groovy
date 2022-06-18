package com.james.all

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.dataobject.BankAccount
import com.james.concurrency.mapper.BankAccountMapper
import com.james.concurrency.service.BankAccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SpringBootTest(classes = ConcurrencyApplication.class)
class AtomicSpec extends Specification {

    @Autowired
    BankAccountService bankAccountService;

    @Autowired
    BankAccountMapper bankAccountMapper;

    def "不区分事务隔离环境，应用层使用了非原子性的代码，造成消费总数减少"() {
        given:
        // 初始值500元
        bankAccountMapper.updateBalanceById(500,1);
        ExecutorService service = Executors.newFixedThreadPool(100);

        // 100次消费，每次5元，业务期望最终账户剩余0元
        for (int i = 0; i < 100; i++) {
            service.execute(() -> bankAccountService.consume(1L, 5));
        }
        service.shutdown();
        while (! service.isTerminated());

        expect:
        // 消费500次，最终账户剩余大于0元，则非原子性的代码存在隐患
        BankAccount bankAccount = bankAccountMapper.selectById(1L);
        def balance = bankAccount.getBalance();
        println(balance);
        balance > 0;
    }
}
