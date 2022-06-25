package com.james.concurrency.repeatableread

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.dataobject.Doctor
import com.james.concurrency.mapper.DoctorMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 《数据密集型应用系统设计》指出了一种写倾斜的隐患
 *  成立条件：
 *      两个事务读取同一组对象，然后更新其中的一部分，不同的对象更新不同的部分，则可能发生写倾斜
 *
 */
@SpringBootTest(classes = ConcurrencyApplication.class)
class WriteSkewSpec extends Specification{

    @Autowired
    DoctorMapper doctorMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    String alice
    String bob
    Long shiftId

    def setup() {
        alice = "Alice";
        bob = "Bob";
        shiftId = 1234;

        doctorMapper.deleteByName(alice);
        doctorMapper.deleteByName(bob);
        // "Alice" 和 "Bob" 都在值班
        doctorMapper.insert(new Doctor(shiftId, alice,true));
        doctorMapper.insert(new Doctor(shiftId, bob,true));
    }

    def "write skew 写倾斜 SET @@GLOBAL.transaction_isolation = 'REPEATABLE-READ'"() {
        given:
        // 线程(事务)A
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    def onCallCount = doctorMapper.countByShiftIdAndOnCall(shiftId, true);
                    Thread.sleep(2000);
                    if (onCallCount > 1) {
                        doctorMapper.updateOnCallByShiftIdAndName(false, shiftId, bob);
                    }
                }
            });
        });

        // 线程(事务)B
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    def onCallCount = doctorMapper.countByShiftIdAndOnCall(shiftId, true);
                    Thread.sleep(2000);
                    if (onCallCount > 1) {
                        doctorMapper.updateOnCallByShiftIdAndName(false, shiftId, alice);
                    }
                }
            });
        });
        executorService.shutdown();
        while(! executorService.isTerminated());

        expect:
        // alice 和 bob 都觉得对方没请假，所以自己都请假了。造成了最后无人值班
        doctorMapper.countByShiftIdAndOnCall(shiftId, true) == 0;
    }
}
