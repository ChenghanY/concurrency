package com.james.concurrency.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseStaticClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClass.class);

    public static void publicStaticMethod() {
        LOGGER.info("publicStaticMethod");
    }
}
