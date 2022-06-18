package com.james.concurrency.controller;

import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.BankAccountService;
import com.james.concurrency.service.impl.BankAccountServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Controller
public class BankAccountController {

    @Autowired
    BankAccountService bankAccountService;

    @Autowired
    BankAccountMapper bankAccountMapper;

    @RequestMapping("/consume")
    public String consume() throws InterruptedException {

        return "success";
    }
}
