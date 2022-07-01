package com.james.concurrency.base.listener;

import com.james.concurrency.dataobject.AccountEvent;
import com.james.concurrency.dataobject.BankAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountModifyBalanceEventListener implements AccountEventListener {

    private final Logger LOGGER = LoggerFactory.getLogger(AccountModifyNameEventListener.class);

    @Override
    public void onBegin(AccountEvent event) {
        BankAccount source = (BankAccount) event.getSource();
        LOGGER.info(">> ModifyBalance begin event ,balance: {}", source.getBalance());
    }

    @Override
    public void onEnd(AccountEvent event) {
        BankAccount source = (BankAccount) event.getSource();
        LOGGER.info(">> ModifyBalance end event ,balance: {}", source.getBalance());
    }
}
