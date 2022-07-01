package com.james.concurrency.controller;

import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ThreadLocalController {

    @Autowired
    BankAccountService service;

    @RequestMapping("/bank/account")
    public ModelAndView doSomething(@RequestParam("id") Long id) {
        // service.doSomething
        return new ModelAndView();
    }

    @RequestMapping("/bank/customer")
    @ResponseBody
    public ModelAndView doSomething(@RequestBody BankAccount bankAccount) {
        // service.doSomething
        return new ModelAndView();
    }

}
