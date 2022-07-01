package com.james.concurrency

import com.james.concurrency.base.listener.BankAccountBankAccountConsumeEventListener
import com.james.concurrency.base.BankAccountConsumeEventBus
import com.james.concurrency.base.listener.LoggerBankAccountConsumeEventListener
import com.james.concurrency.base.listener.SimpleBankAccountConsumeEventListener
import spock.lang.Specification

class JavaEventSpec extends Specification{

    def "Java 原生事件api用法"() {
        expect:
        BankAccountConsumeEventBus publisher = new BankAccountConsumeEventBus();
        // 添加方法监听器, 对监听器的扩展是开放的，而不用改核心的 methodToMonitor 方法
        publisher.addMethodExecutionEventListener(new SimpleBankAccountConsumeEventListener());
        publisher.addMethodExecutionEventListener(new BankAccountBankAccountConsumeEventListener());
        publisher.addMethodExecutionEventListener(new LoggerBankAccountConsumeEventListener());

        publisher.publishConsumeEvent();
        // 客户端随时移除掉已有的监听器避免内存泄露
        publisher.removeAllListeners();
    }
}
