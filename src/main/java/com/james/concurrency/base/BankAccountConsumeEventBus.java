package com.james.concurrency.base;

import com.james.concurrency.base.listener.BankAccountConsumeEventListener;
import com.james.concurrency.dataobject.BankAccount;
import com.james.concurrency.dataobject.ConsumeEvent;
import com.james.concurrency.dataobject.MethodExecutionStatus;
import net.minidev.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * 银行消费的事件总线（用于发布事件和管理订阅者）
 */
public class BankAccountConsumeEventBus {

    /**
     * 账户消费的事件，可以被多人订阅
     */
    private final List<BankAccountConsumeEventListener> listeners = new ArrayList<>();

    /**
     *
     */
    public void publishConsumeEvent(BankAccount bankAccount) {
        ConsumeEvent event2Publish = new ConsumeEvent(bankAccount);
        publishEvent(MethodExecutionStatus.BEGIN, event2Publish);
        System.out.println("消费事件，账户发生变化: " + bankAccount);
        publishEvent(MethodExecutionStatus.END, event2Publish);
    }

    private void publishEvent(MethodExecutionStatus status, ConsumeEvent event2Publish) {
        // 对所有订阅者发送事件
        List<BankAccountConsumeEventListener> copyListeners = new ArrayList<>(listeners);
        for (BankAccountConsumeEventListener listener : copyListeners) {
            if (MethodExecutionStatus.BEGIN.equals(status)) {
                listener.onConsumeBegin(event2Publish);
            } else if (MethodExecutionStatus.END.equals(status)) {
                listener.onConsumeEnd(event2Publish);
            }
        }
    }

    public void addMethodExecutionEventListener(BankAccountConsumeEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeAllListeners() {
        this.listeners.clear();
    }

}
