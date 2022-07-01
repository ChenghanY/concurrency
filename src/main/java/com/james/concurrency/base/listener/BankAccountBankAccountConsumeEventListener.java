package com.james.concurrency.base.listener;

import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.dataobject.ConsumeEvent;

public class BankAccountBankAccountConsumeEventListener implements BankAccountConsumeEventListener {

    @Override
    public void onConsumeBegin(ConsumeEvent event) {
        BankAccount source = (BankAccount) event.getSource();
        System.out.println("BankAccount finish to execute the consume [" + source.getName() + "]");
    }

    @Override
    public void onConsumeEnd(ConsumeEvent event) {
        BankAccount source = (BankAccount) event.getSource();
        System.out.println("BankAccount finish to execute the method [" + source.getName()+ "]");
    }
}
