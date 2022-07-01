package com.james.concurrency.base.listener;

import com.james.concurrency.dataobject.ConsumeEvent;

import java.util.EventListener;

public interface BankAccountConsumeEventListener extends EventListener {

    /**
     * 监听消费开始事件
     */
    void onConsumeBegin(ConsumeEvent event);

    /**
     * 监听消费结束事件
     */
    void onConsumeEnd(ConsumeEvent event);
}
