package com.james.concurrency.controller;

import com.james.concurrency.dataobject.BankAccount;

public class BankAccountRequestHolder {

    public static ThreadLocal<BankAccount> holder = new ThreadLocal<>();

}
