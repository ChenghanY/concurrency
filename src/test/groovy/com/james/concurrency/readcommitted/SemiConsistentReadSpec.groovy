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

/**
 * 官方原文：https://dev.mysql.com/doc/refman/5.7/en/glossary.html
 * 参考：https://juejin.cn/post/6844904022499917838
 *
 * semi-consistent read
 * A type of read operation used for UPDATE statements, that is a combination of READ COMMITTED and consistent read.
 * When an UPDATE statement examines a row that is already locked, InnoDB returns the latest committed version to MySQL
 * so that MySQL can determine whether the row matches the WHERE condition of the UPDATE.
 * If the row matches (must be updated), MySQL reads the row again,
 * and this time InnoDB either locks it or waits for a lock on it.
 * This type of read operation can only happen when the transaction has the READ COMMITTED isolation level,
 * or when the innodb_locks_unsafe_for_binlog option is enabled. innodb_locks_unsafe_for_binlog was removed in MySQL 8.0.
 *
 * TODO
 */
@SpringBootTest(classes = ConcurrencyApplication.class)
class SemiConsistentReadSpec extends Specification {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    List<BankAccount> beforeList;


}
