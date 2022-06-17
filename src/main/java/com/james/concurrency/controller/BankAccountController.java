package com.james.concurrency.controller;

import com.james.concurrency.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BankAccountController {

    @Autowired
    BankAccountService bankAccountService;

    @RequestMapping("/consume")
    public String consume(@RequestParam(name = "id") Long id, @RequestParam(name = "cost") int cost)  {
        bankAccountService.consume(id, cost);
        return "success";
    }
}
