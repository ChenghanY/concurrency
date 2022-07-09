package com.james.concurrency.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubClassCanNotReduceScope extends BaseClass{

    private final Logger LOGGER = LoggerFactory.getLogger(SubClassCanNotReduceScope.class);

    /*@Override
    protected void publicMethod() { // 子类是public 子类不能缩小范围
        super.publicMethod();
    }*/

    /**
     * 可以把protected权限放大
     */
    @Override
    public void protectedMethod() {
        LOGGER.info("protectedMethod ");
        super.protectedMethod();
    }
}
