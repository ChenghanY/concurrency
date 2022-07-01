package com.james.concurrency.base.listener;

import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.dataobject.AccountEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountModifyNameEventListener implements AccountEventListener {

    private final Logger LOGGER = LoggerFactory.getLogger(AccountModifyNameEventListener.class);

    @Override
    public void onBegin(AccountEvent event) {
        BankAccount source = (BankAccount) event.getSource();
        LOGGER.info(">> ModifyName begin event ,name: {}", source.getName());
    }

    @Override
    public void onEnd(AccountEvent event) {
        BankAccount source = (BankAccount) event.getSource();
        LOGGER.info(">> ModifyName end event ,name: {}", source.getName());
    }
}
