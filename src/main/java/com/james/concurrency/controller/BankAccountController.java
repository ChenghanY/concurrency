package com.james.concurrency.controller;

import com.james.concurrency.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BankAccountController {

    int count = 0;

    @Autowired
    BankAccountService bankAccountService;

    @RequestMapping("/consume")
    public String consume(@RequestParam(name = "id") Long id, @RequestParam(name = "cost") int cost)  {
        if (count == 0) {
            count ++;
            bankAccountService.consume(id, cost, () -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {

                }
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            });
        } else {
            bankAccountService.consume(id, cost, () -> {});
        }
        return "success";
    }
}
