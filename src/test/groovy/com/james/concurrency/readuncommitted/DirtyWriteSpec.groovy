package com.james.concurrency.readuncommitted

import com.james.concurrency.ConcurrencyApplication
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = ConcurrencyApplication.class)
class DirtyWriteSpec extends Specification {



}
