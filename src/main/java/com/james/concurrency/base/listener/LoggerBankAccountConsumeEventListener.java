package com.james.concurrency.base.listener;

import com.james.concurrency.dataobject.ConsumeEvent;

public class LoggerBankAccountConsumeEventListener implements BankAccountConsumeEventListener {

    @Override
    public void onConsumeBegin(ConsumeEvent event) {
    }

    @Override
    public void onConsumeEnd(ConsumeEvent event) {
    }
}
