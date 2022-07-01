package com.james.concurrency.base;

import com.james.concurrency.base.listener.AccountEventListener;
import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.dataobject.AccountEvent;
import com.james.concurrency.dataobject.EventStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 账户的事件总线（用于发布事件和管理订阅者）
 */
public class AccountEventBus {

    /**
     * 账户变动的事件，可以被多人订阅。每个人订阅的关注点可以相同也可以不同
     */
    private final List<AccountEventListener> listeners = new ArrayList<>();

    /**
     * 事件发出，订阅者逻辑被依次调用
     */
    public void publishEvent(BankAccount bankAccount) {
        AccountEvent event2Publish = new AccountEvent(bankAccount);
        publishEventByStatus(EventStatus.BEGIN, event2Publish);
        System.out.println("账户发生变化，变更后的值为：" + bankAccount);
        publishEventByStatus(EventStatus.END, event2Publish);
    }

    private void publishEventByStatus(EventStatus status, AccountEvent event2Publish) {
        // 对所有订阅者发送事件
        List<AccountEventListener> copyListeners = new ArrayList<>(listeners);
        for (AccountEventListener listener : copyListeners) {
            if (EventStatus.BEGIN.equals(status)) {
                listener.onBegin(event2Publish);
            } else if (EventStatus.END.equals(status)) {
                listener.onEnd(event2Publish);
            }
        }
    }

    public void addMethodExecutionEventListener(AccountEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeAllListeners() {
        this.listeners.clear();
    }

}
