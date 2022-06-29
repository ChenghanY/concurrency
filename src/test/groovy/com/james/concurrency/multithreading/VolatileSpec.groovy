package com.james.concurrency.multithreading

import com.james.concurrency.ConcurrencyApplication
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

/**
 * volatile 轻量级同步工具，用于避免指令重排和可见性隐患。
 * volatile 在非原子操作时不保证同步，如i++
 *
 * 避免指令重排：
 * lazyDoubleCheckSingleton = new LazyDoubleCheckSingleton(); 这个语句执行了三个步骤，
 * 1 创建空的资源对象, 生成内存地址
 * 2 调用class文件的构造方法
 * 3 讲内存地址赋值给lazyDoubleCheckSingleton
 * note : 2 和 3 会产生重排序，把一个没初始化好的对象发布出去了，可能触发NPE
 *
 *
 * 可见性隐患：
 * 一写多读，多读不一致（）
 */
@SpringBootTest(classes = ConcurrencyApplication.class)
class VolatileSpec extends Specification{

}
