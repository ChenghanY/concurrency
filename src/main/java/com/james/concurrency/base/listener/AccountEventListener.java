package com.james.concurrency.base.listener;

import com.james.concurrency.dataobject.AccountEvent;

import java.util.EventListener;

public interface AccountEventListener extends EventListener {

    /**
     * 监听消费开始事件
     */
    void onBegin(AccountEvent event);

    /**
     * 监听消费结束事件
     */
    void onEnd(AccountEvent event);
}
