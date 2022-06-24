package com.james.concurrency.readcommitted

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.dataobject.BankAccount
import com.james.concurrency.mapper.BankAccountMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SpringBootTest(classes = ConcurrencyApplication.class)
class DirtyReadFixSpec extends Specification{

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    def setup () {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED)
    }

    def "解决脏读 SET @@GLOBAL.transaction_isolation = 'READ-COMMITTED' 及以上的隔离级别"() {
        given:
        bankAccountMapper.updateBalanceById(0, 1L);
        // 线程(事务)A
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    BankAccount bankAccount = bankAccountMapper.selectById(1L);
                    // 1. 线程A写入脏数据
                    bankAccountMapper.updateBalanceById(bankAccount.getBalance() + 1, 1L);
                    // 3. 制造脏数据被使用的间隙
                    Thread.sleep(3000);
                    // 6. 线程A回滚
                    status.setRollbackOnly();
                }
            });
        });

        // 线程(事务)B
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    // 2. 等待脏数据写入
                    Thread.sleep(1000);
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
        // rc及以上隔离级别避免脏读
        bankAccountMapper.selectById(1L).getBalance() == 1
    }
}
