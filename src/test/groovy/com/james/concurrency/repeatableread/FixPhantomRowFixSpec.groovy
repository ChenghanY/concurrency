package com.james.concurrency.repeatableread

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

@SpringBootTest(classes = ConcurrencyApplication.class)
class FixPhantomRowFixSpec extends Specification  {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    def setup() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ)

        bankAccountMapper.updateBalanceById(300, 1L);
        bankAccountMapper.updateBalanceById(500, 2L);
        bankAccountMapper.updateBalanceById(600, 3L);
        bankAccountMapper.deleteByName("rose");
    }

    def "幻读解决： 普通select(快照隔离) + SET @@GLOBAL.transaction_isolation = 'REPEATABLE-READ' 及以上" () {
        given:
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
        // REPEATABLE-READ 级别下使用了快照隔离，不用for update 也能解决 phantom row 问题。但是读的不是实时数据
        bankAccounts.get(0).getBalance() == bankAccounts.get(1).getBalance()
    }

    def "幻读解决： update + SET @@GLOBAL.transaction_isolation = 'REPEATABLE-READ' 及以上" () {
        given:
        List<BankAccount> beforeList = bankAccountMapper.selectListByBalanceGt(500);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        // 线程(事务)A使用了两次范围更新
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    // 1. 范围更新balance字段
                    bankAccountMapper.updateBalanceByBalanceGt(999,500);
                    Thread.sleep(2000);
                    // 3. 范围更新balance字段
                    bankAccountMapper.updateNameByBalanceGt("newName",500);
                }
            });
        } as Runnable);

        // 线程(事务)B中途修改数据
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Thread.sleep(1000);
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

        expect:
        // gap锁生效，在间隙中插入数据需要等待。所以事务A两次范围更新的数量一致
        bankAccountMapper.selectListByBalance(999).size()
                == bankAccountMapper.selectListByName("newName").size();

        cleanup:
        // 恢复修改前的数据
        beforeList.forEach(e -> bankAccountMapper.updateById(e));
        bankAccountMapper.deleteByName("rose");
    }
}
