package com.james.concurrency.dataobject;

import java.util.EventObject;

public class ConsumeEvent extends EventObject {

    public ConsumeEvent(Object source) {
        super(source);
    }

}
