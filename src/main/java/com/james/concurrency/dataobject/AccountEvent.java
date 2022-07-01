package com.james.concurrency.dataobject;

import java.util.EventObject;

public class AccountEvent extends EventObject {

    public AccountEvent(BankAccount source) {
        super(source);
    }

}
