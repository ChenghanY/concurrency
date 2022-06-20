package com.james.concurrency.all

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
class LostUpdateSpec extends Specification {

    @Autowired
    BankAccountService bankAccountService;

    @Autowired
    BankAccountMapper bankAccountMapper;

    def "任意隔离级别下，会存在更新丢失"() {
        given:
        int concurrent_count = 100;
        bankAccountMapper.updateBalanceById(concurrent_count,1);
        ExecutorService service = Executors.newFixedThreadPool(concurrent_count);

        // 每次消费1元，业务期望最终账户剩余0元
        for (int i = 0; i < concurrent_count; i++) {
            service.execute(() -> bankAccountService.consume(1L, 1));
        }
        service.shutdown();
        while (! service.isTerminated());

        expect:
        // 最终账户剩余大于0元，则非原子性的代码存在隐患 ---- 更新丢失
        BankAccount bankAccount = bankAccountMapper.selectById(1L);
        bankAccount.getBalance() > 0;
    }

    def "任意隔离级别下，使用原子性代码,2000次并发无异常" () {
        given:
        int concurrent_count = 2000;
        bankAccountMapper.updateBalanceById(concurrent_count,1);
        ExecutorService service = Executors.newFixedThreadPool(concurrent_count);

        for (int i = 0; i < concurrent_count; i++) {
            service.execute(() -> bankAccountService.atomicConsume(1L, 1));
        }
        service.shutdown();
        while (! service.isTerminated());

        expect:
        BankAccount bankAccount = bankAccountMapper.selectById(1L);
        bankAccount.getBalance() == 0;
    }

    def "任意隔离级别下，应用层使用forUpdate加锁,2000次并发无异常" () {
        given:
        int concurrent_count = 2000;
        bankAccountMapper.updateBalanceById(concurrent_count,1);
        ExecutorService service = Executors.newFixedThreadPool(concurrent_count);

        for (int i = 0; i < concurrent_count; i++) {
            service.execute(() -> bankAccountService.forUpdateConsume(1L, 1));
        }
        service.shutdown();
        while (! service.isTerminated());

        expect:
        BankAccount bankAccount = bankAccountMapper.selectById(1L);
        bankAccount.getBalance() == 0;
    }

    def "任意隔离级别下，应用层使用lock in share mode加共享锁,100次并发，发生死锁" () {
        given:
        int concurrent_count = 2000;
        bankAccountMapper.updateBalanceById(concurrent_count,1);
        ExecutorService service = Executors.newFixedThreadPool(concurrent_count);

        for (int i = 0; i < concurrent_count; i++) {
            service.execute(() -> bankAccountService.lockInShareModeConsume(1L, 1));
        }
        service.shutdown();
        while (! service.isTerminated());

        expect:
        BankAccount bankAccount = bankAccountMapper.selectById(1L);
        bankAccount.getBalance() == 0;
        // 控制台报 MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
        // 运行时可以用 SHOW ENGINE INNODB STATUS; 查看死锁信息

        /*
            --------------------------------------------------------------------------------
            知识点1:
                加锁读 lock in share mode
                https://dev.mysql.com/doc/refman/5.7/en/innodb-locking-reads.html

            原文:
             lock in share mode:
             Sets a shared mode lock on any rows that are read. Other sessions can read the rows,
             but cannot modify them until your transaction commits.
            释义:
             lock in share mode 读的时候加锁，加锁期间其他事务可以读不可以写。
             lock in share mode  持有锁，直到事务提交才释放锁。

            --------------------------------------------------------------------------------
            知识点2:
                运行时用SHOW ENGINE INNODB STATUS看日志，明确 lock in share mode 加 s 锁 （读锁）, update 加 x 锁 （写锁）
                死锁官方例子: https://dev.mysql.com/doc/refman/5.7/en/innodb-deadlock-example.html

                按下面顺序加锁会导致死锁
                A事务 lock in share mode =》 获取读锁成功
                B事务 lock in share mode =》 获取读锁成功
                B事务 update 期望获得锁升级，尝试获取写锁，被A事务的读锁阻塞。 =》 等待A读锁释放
                A事务 update 期望获得锁升级，尝试获取写锁，被B事务的读锁阻塞。 =》 等待B读锁释放

                整理一下:
                A事务持有读锁不释放，等待B事务释放读锁
                B事务持有读锁不释放，等待A事务释放读锁
                死锁发生。

                证明(抽取Mysql日志关键信息):
                (1) TRANSACTION:
                WAITING FOR THIS LOCK TO BE GRANTED: RECORD LOCKS lock_mode X 【A事务 update  被B事务的读锁阻塞。】

                (2) TRANSACTION:
                HOLDS THE LOCK(S): RECORD LOCKS lock mode S   【B事务 lock in share mode】
                WAITING FOR THIS LOCK TO BE GRANTED: RECORD LOCKS lock_mode X 【 B事务 update  被A事务的读锁阻塞。】
         */
    }
}