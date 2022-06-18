package com.james.concurrency.readuncommitted

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.dataobject.BankAccount
import com.james.concurrency.mapper.BankAccountMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SpringBootTest(classes = ConcurrencyApplication.class)
class DirtyReadSpec extends Specification {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    def "read uncommitted 隔离级别下产生脏读"() {
        given:
        bankAccountMapper.updateBalanceById(0, 1L);
        // 1. 新建程写入脏数据
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    BankAccount bankAccount = bankAccountMapper.selectById(1L);
                    bankAccountMapper.updateBalanceById(bankAccount.getBalance() + 1, 1L);
                    // 3. 制造脏数据被使用的间隙
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    status.setRollbackOnly();
                }
            });
        });

        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    // 2. 等待脏数据写入
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 4. 脏数据被读取
                    BankAccount bankAccount = bankAccountMapper.selectById(1L);
                    // 5. 脏数据被写入
                    bankAccountMapper.updateBalanceById(bankAccount.getBalance() + 1, 1L);
                }
            });
        });
        executorService.shutdown();
        while(! executorService.isTerminated());

        expect:
        // 虽然回滚了，但是发生脏读，总数为2
        bankAccountMapper.selectById(1L).getBalance() == 2
    }
}
