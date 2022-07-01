package com.james.concurrency

import com.james.concurrency.base.listener.AccountModifyNameEventListener
import com.james.concurrency.base.AccountEventBus
import com.james.concurrency.base.listener.AccountModifyBalanceEventListener
import com.james.concurrency.dataobject.BankAccount
import com.james.concurrency.mapper.BankAccountMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = ConcurrencyApplication.class)
class JavaEventSpec extends Specification{

    @Autowired
    BankAccountMapper bankAccountMapper

    def setup() {
        bankAccountMapper.deleteByName("james")
    }

    def "Java 原生事件api用法"() {
        given:
        def newAccount = new BankAccount("james", 200)

        when:
        // 新增操作
        bankAccountMapper.insert(newAccount)
        AccountEventBus bus = new AccountEventBus();
        // 添加监听器, 需要添加更多的监听器只用增加AccountEventListener的实现类即可不用关心Bus的实现，对拓展开放
        bus.addMethodExecutionEventListener(new AccountModifyBalanceEventListener());
        bus.addMethodExecutionEventListener(new AccountModifyNameEventListener());

        then:
        // 发布事件，所有监听器响应事件
        bus.publishEvent(newAccount);
        // 客户端随时移除掉已有的监听器避免内存泄露
        bus.removeAllListeners();
    }
}
