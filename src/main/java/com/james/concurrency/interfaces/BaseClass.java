package com.james.concurrency.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseClass {

    private final Logger LOGGER = LoggerFactory.getLogger(BaseClass.class);

    public void publicMethod() {
        LOGGER.info("publicMethod");
    }

    protected void protectedMethod() {
        LOGGER.info("protectedMethod");
    }

    public void publicMethod(int i) {
        LOGGER.info("[重载方法可以发生在任意地方");
    }

    public int publicMethod(int i, int b) {
        LOGGER.info("重载方法可以发生在任意地方");
        return 1;
    }

    private void privateMethod() {
        LOGGER.info("privateMethod");
    }
}
