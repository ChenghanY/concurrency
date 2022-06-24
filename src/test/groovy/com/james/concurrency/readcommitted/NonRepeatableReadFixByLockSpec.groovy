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

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * 官网定义的consistent_read：
 * https://dev.mysql.com/doc/refman/5.7/en/innodb-consistent-read.html
 * https://dev.mysql.com/doc/refman/5.7/en/glossary.html#glos_consistent_read
 *
 * A consistent read means that InnoDB uses multi-versioning
 * to present to a query a snapshot of the database at a point in time.
 * 【释义】：
 *  一致性读：innodb 向查询请求（select）提供数据库在某个时间点的快照。这种建立快照的技术使用的是多版本控制。
 *
 * If the transaction isolation level is REPEATABLE READ (the default level), all consistent reads
 * within the same transaction read the snapshot established by the first such read in that transaction.
 * With READ COMMITTED isolation level, the snapshot is reset to the time of each consistent read operation.
 * 【释义】：
 *  隔离级别为REPEATABLE READ, 在一个事务中, 第一次的一致性读便建立快照。在这个事务中后续的一致性读都是同一个快照的内容（有例外后续讨论）
 *  隔离级别为READ COMMITTED，每个一致性读操作都会建立新的快照。
 *
 * Consistent read is the default mode in which InnoDB processes SELECT statements in
 * READ COMMITTED and REPEATABLE READ isolation levels.
 * Because a consistent read does not set any locks on the tables it accesses,
 * other sessions are free to modify those tables while a consistent read is being performed on the table.
 * 【释义】：
 *  一次性读是 REPEATABLE READ 和 READ COMMITTED 的默认模型。换言之，在这两个隔离级别下，普通的select都是一次性读。
 *  这种基于快照的一次性读的好处是：不是锁操作，读取数据的情况下其他事务可以修改数据。
 *
 *
 * 本用例验证 READ COMMITTED 会产生（不可重复读）NonRepeatableRead的隐患。并用加锁的方式解决该隐患
 */
@SpringBootTest(classes = ConcurrencyApplication.class)
class NonRepeatableReadFixByLockSpec extends Specification {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    def setup () {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED)
    }

    def "不可重复读：SET @@GLOBAL.transaction_isolation = 'READ-COMMITTED'"() {
        given:

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        // 线程(事务)A重复读取
        Future<List<BankAccount>> future = executorService.submit(() -> {
            return transactionTemplate.execute(status -> {
                List<BankAccount> result = new ArrayList<>();
                // 1. 第一次查
                BankAccount first = bankAccountMapper.selectById(1L);
                Thread.sleep(3000);
                // 3. 第二次查
                BankAccount second = bankAccountMapper.selectById(1L);
                result.add(first);
                result.add(second);
                return result;
            });
            // 由于使用Groovy闭包最好指定类型，记得加as Callable
        } as Callable) as Future<List<BankAccount>>;

        // 线程(事务)B中途修改数据
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Thread.sleep(1000);
                    // 2. 修改数据并提交
                    bankAccountMapper.atomicUpdateBalanceByCostAndId(1, 1L);
                }
            });
        } as Runnable);

        List<BankAccount> bankAccounts = future.get();
        executorService.shutdown();
        while (!executorService.isTerminated()) ;

        expect:
        // 同一个事务中，两次读取数据不一致，则为不可重复读
        bankAccounts.get(0).getBalance() != bankAccounts.get(1).getBalance();
    }

    def "解决不可重复读：使用for update排他锁 SET @@GLOBAL.transaction_isolation = 'READ-COMMITTED'"() {
        given:

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        // 线程(事务)A重复读取
        Future<List<BankAccount>> future = executorService.submit(() -> {
            return transactionTemplate.execute(status -> {
                List<BankAccount> result = new ArrayList<>();
                // 1. 第一次查(for update)
                BankAccount first = bankAccountMapper.selectByIdForUpdate(1L);
                Thread.sleep(3000);
                // 3. 第二次查(for update)
                BankAccount second = bankAccountMapper.selectByIdForUpdate(1L);
                result.add(first);
                result.add(second);
                return result;
            });
            // 由于使用Groovy闭包最好指定类型，记得加as Callable
        } as Callable) as Future<List<BankAccount>>;

        // 线程(事务)B中途修改数据
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Thread.sleep(1000);
                    // 2. 修改数据并提交
                    bankAccountMapper.atomicUpdateBalanceByCostAndId(1, 1L);
                }
            });
        } as Runnable);

        List<BankAccount> bankAccounts = future.get();
        executorService.shutdown();
        while (!executorService.isTerminated()) ;

        expect:
        bankAccounts.get(0).getBalance() == bankAccounts.get(1).getBalance();
    }
}
