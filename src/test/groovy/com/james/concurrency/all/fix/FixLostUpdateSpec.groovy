package com.james.concurrency.all.fix

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.dataobject.BankAccount
import com.james.concurrency.mapper.BankAccountMapper
import com.james.concurrency.service.BankAccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * {@link com.james.concurrency.all.LostUpdateSpec 验证了更新丢失的存在} 这里提出解决方法
 */
@SpringBootTest(classes = ConcurrencyApplication.class)
class FixLostUpdateSpec extends Specification {

    @Autowired
    BankAccountService bankAccountService;

    @Autowired
    BankAccountMapper bankAccountMapper;

    def "更新丢失解决：任意隔离级别下，使用原子性代码，2000次并发无异常" () {
        given:
        int concurrent_count = 2000;
        bankAccountMapper.updateBalanceById(concurrent_count,1);
        ExecutorService service = Executors.newFixedThreadPool(concurrent_count);

        for (int i = 0; i < concurrent_count; i++) {
            service.execute(() -> bankAccountService.atomicConsume(1, 1L));
        }
        service.shutdown();
        while (! service.isTerminated());

        expect:
        BankAccount bankAccount = bankAccountMapper.selectById(1L);
        bankAccount.getBalance() == 0;
    }

    def "更新丢失解决：任意隔离级别下，应用层使用forUpdate加锁,2000次并发无异常" () {
        given:
        int concurrent_count = 2000;
        bankAccountMapper.updateBalanceById(concurrent_count,1);
        ExecutorService service = Executors.newFixedThreadPool(concurrent_count);

        for (int i = 0; i < concurrent_count; i++) {
            service.execute(() -> bankAccountService.forUpdateConsume(1, 1L));
        }
        service.shutdown();
        while (! service.isTerminated());

        expect:
        BankAccount bankAccount = bankAccountMapper.selectById(1L);
        bankAccount.getBalance() == 0;
    }
}
