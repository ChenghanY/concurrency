package com.james.concurrency.serializable.fix

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
 * {@link com.james.concurrency.repeatableread.WriteSkewSpec 验证写倾斜} 这里给出解决方案
 */
@SpringBootTest(classes = ConcurrencyApplication.class)
class FixWriteSkewSpec extends Specification{

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

    def "写倾斜write skew解决: for update 加锁"() {
        given:
        // 线程(事务)A
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    def onCallCount = doctorMapper.countByShiftIdAndOnCallForUpdate(shiftId, true);
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
                    def onCallCount = doctorMapper.countByShiftIdAndOnCallForUpdate(shiftId, true);
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
        // alice 和 bob 互斥访问目前值班人员的情况，最终只有一个人请假成功
        doctorMapper.countByShiftIdAndOnCall(shiftId, true) == 1;
    }

    def "写倾斜write skew解决: 换序, 先请假成功再查最新的数据最后提交"() {
        given:
        // 线程(事务)A
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    doctorMapper.updateOnCallByShiftIdAndName(false, shiftId, bob);
                    Thread.sleep(2000);
                    def onCallCount = doctorMapper.countByShiftIdAndOnCall(shiftId, true);
                    // 目前没有人值班了，请假失败，回退
                    if (onCallCount < 1) {
                        status.setRollbackOnly();
                    }
                }
            });
        });

        // 线程(事务)B
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    doctorMapper.updateOnCallByShiftIdAndName(false, shiftId, alice);
                    Thread.sleep(2000);
                    def onCallCount = doctorMapper.countByShiftIdAndOnCall(shiftId, true);
                    // 目前没有人值班了，请假失败，回退
                    if (onCallCount < 1) {
                        status.setRollbackOnly();
                    }
                }
            });
        });
        executorService.shutdown();
        while(! executorService.isTerminated());

        expect:
        // alice 和 bob 同一刻都想请假，有一个人请假失败了。则并发安全
        doctorMapper.countByShiftIdAndOnCall(shiftId, true) == 1;
    }
}
