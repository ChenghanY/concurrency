package com.james.concurrency.dataobject;

import java.util.Date;

public class BankAccount {

    private Long id;

    private String name;

    private int balance;

    public BankAccount(Long id, String name, int balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public BankAccount(String name, int balance) {
        this.name = name;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }
}
