package com.james.concurrency.controller;

import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.mapper.BankAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Controller
public class BankAccountController {

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    @RequestMapping("/consume")
    public String consume() throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        // 线程(事务)A重复读取
        Future<List<BankAccount>> future = executorService.submit(() -> {
            return transactionTemplate.execute(status -> {
                List<BankAccount> result = new ArrayList<>();
                // 1. 第一次查
                BankAccount first = bankAccountMapper.selectById(1L);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 3. 第二次查
                BankAccount second = bankAccountMapper.selectById(1L);
                result.add(first);
                result.add(second);
                return result;
            });
            // 由于使用Groovy闭包最好指定类型，记得加as Callable
        });

        // 线程(事务)B中途修改数据
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 2. 修改数据并提交
                    bankAccountMapper.atomicUpdateBalanceByCostAndId(1, 1L);
                }
            });
        });

        executorService.shutdown();
        while (!executorService.isTerminated()) ;
        List<BankAccount> bankAccounts = future.get();
        for (BankAccount bankAccount : bankAccounts) {
            System.out.println(bankAccount.getBalance());
        }

        return "success";
    }
}
