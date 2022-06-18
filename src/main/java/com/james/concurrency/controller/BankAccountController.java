package com.james.concurrency.controller;

import com.james.concurrency.mapper.BankAccountMapper;
import com.james.concurrency.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
