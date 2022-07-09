package com.james.concurrency.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubClassCanNotOverridePrivate extends BaseClass{

    private final Logger LOGGER = LoggerFactory.getLogger(SubClassCanNotOverridePrivate.class);

    @Override
    public void publicMethod() {
        LOGGER.info("子类继承父类方法");
        super.publicMethod();
    }

    @Override
    protected void protectedMethod() {
        LOGGER.info("子类继承父类方法");
        super.protectedMethod();
    }

    // private 方法互相独立，加 @Override 会报错
    private void privateMethod() {
        LOGGER.info("privateMethod");
    }
}
