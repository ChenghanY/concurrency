package com.james.concurrency.readcommitted

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.dataobject.BankAccount
import com.james.concurrency.mapper.BankAccountMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * 官方文档：https://dev.mysql.com/doc/refman/5.7/en/innodb-next-key-locking.html
 *
 * 原文：
 * The so-called phantom problem occurs within a transaction when the same query produces different sets of rows
 * at different times. For example, if a SELECT is executed twice,
 * but returns a row the second time that was not returned the first time, the row is a “phantom” row.
 * > 根据官网描述，这里把 phantom row 称为幻读
 *
 * 原文：
 * To prevent phantoms, InnoDB uses an algorithm called next-key locking
 * that combines index-row locking with gap locking.
 * > InnoDB 用next-key锁（索引锁 + 间隙锁）防止幻读
 *
 * 原文：
 * Gap locking can be disabled explicitly. This occurs if you change the transaction isolation level to READ COMMITTED
 * or enable the innodb_locks_unsafe_for_binlog system variable (which is now deprecated).
 * In this case, gap locking is disabled for searches and index scans and
 * is used only for foreign-key constraint checking and duplicate-key checking.
 * > RC隔离级别下，间隙锁被禁用，所以无法解决幻读的隐患。
 *
 * 值得一提的是，RC下使用for update 用锁解决了同一数据的不可重复读问题。但是对于范围查询却没办法，这种范围查询的出现视为幻读
 */
@SpringBootTest(classes = ConcurrencyApplication.class)
class PhantomRowSpec extends Specification {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    def "产生幻读： for update + SET @@GLOBAL.transaction_isolation = 'READ-COMMITTED'" () {
        given:
        bankAccountMapper.updateBalanceById(300, 1L);
        bankAccountMapper.updateBalanceById(500, 2L);
        bankAccountMapper.updateBalanceById(600, 3L);
        bankAccountMapper.deleteByName("rose");

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        // 线程(事务)A使用了for update 范围查询
        Future<List<BankAccount>> future = executorService.submit(() -> {
            return transactionTemplate.execute(status -> {
                List<BankAccount> result = new ArrayList<>();
                // 1. for update 查出第一个结果
                List<BankAccount> list4Cal = bankAccountMapper.selectListByBalanceGtForUpdate(500);
                if (list4Cal.size() > 0) {
                    BankAccount firstCalResult = new BankAccount();
                    firstCalResult.setBalance(list4Cal.stream().mapToInt(e -> e.getBalance()).sum());
                    result.add(firstCalResult);
                }
                Thread.sleep(3000);
                // 3. for update 查出第二个结果
                List<BankAccount> list4Cal2 = bankAccountMapper.selectListByBalanceGtForUpdate(500);
                if (list4Cal2.size() > 0) {
                    BankAccount secondCalResult = new BankAccount();
                    secondCalResult.setBalance(list4Cal2.stream().mapToInt(e -> e.getBalance()).sum());
                    result.add(secondCalResult);
                }
                return result;
            });
            // 由于使用Groovy闭包最好指定类型，记得加as Callable否则会认为是Runnable，造成future.get()空指针。
        } as Callable) as Future<List<BankAccount>>;

        // 线程(事务)B中途修改数据
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Thread.sleep(2000);
                    BankAccount newAccount = new BankAccount();
                    newAccount.setBalance(550);
                    newAccount.setName("rose")
                    // 2.在间隙中插入数据
                    bankAccountMapper.insert(newAccount);
                }
            });
        } as Runnable);

        executorService.shutdown();
        while (!executorService.isTerminated());
        List<BankAccount> bankAccounts = future.get();

        expect:
        // 值得一提的是，RC下使用for update 用锁解决了同一数据的不可重复读问题。但是对于范围查询却没办法，这种范围查询的出现视为幻读
        bankAccounts.get(0).getBalance() != bankAccounts.get(1).getBalance()
    }

    def "幻读解决： 普通select(快照隔离) + SET @@GLOBAL.transaction_isolation = 'REPEATABLE-READ'" () {
        given:
        bankAccountMapper.updateBalanceById(300, 1L);
        bankAccountMapper.updateBalanceById(500, 2L);
        bankAccountMapper.deleteByName("rose");

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        // 线程(事务)A使用了for update 范围查询
        Future<List<BankAccount>> future = executorService.submit(() -> {
            return transactionTemplate.execute(status -> {
                List<BankAccount> result = new ArrayList<>();
                // 1. for update 查出第一个结果
                List<BankAccount> list4Cal = bankAccountMapper.selectListByBalanceGt(500);
                if (list4Cal.size() > 0) {
                    BankAccount firstCalResult = new BankAccount();
                    firstCalResult.setBalance(list4Cal.stream().mapToInt(e -> e.getBalance()).sum());
                    result.add(firstCalResult);
                }
                Thread.sleep(3000);
                // 3. for update 查出第二个结果
                List<BankAccount> list4Cal2 = bankAccountMapper.selectListByBalanceGt(500);
                if (list4Cal2.size() > 0) {
                    BankAccount secondCalResult = new BankAccount();
                    secondCalResult.setBalance(list4Cal.stream().mapToInt(e -> e.getBalance()).sum());
                    result.add(secondCalResult);
                }
                return result;
            });
            // 由于使用Groovy闭包最好指定类型，记得加as Callable否则会认为是Runnable，造成future.get()空指针。
        } as Callable) as Future<List<BankAccount>>;

        // 线程(事务)B中途修改数据
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Thread.sleep(2000);
                    BankAccount newAccount = new BankAccount();
                    newAccount.setBalance(550);
                    newAccount.setName("rose")
                    // 2.在间隙中插入数据
                    bankAccountMapper.insert(newAccount);
                }
            });
        } as Runnable);

        executorService.shutdown();
        while (!executorService.isTerminated());
        List<BankAccount> bankAccounts = future.get();

        expect:
        // REPEATABLE-READ 级别下使用了快照隔离，不用for update 也能解决 phantom row 问题。但是读的不是实时数据
        bankAccounts.get(0).getBalance() == bankAccounts.get(1).getBalance()
    }
}
