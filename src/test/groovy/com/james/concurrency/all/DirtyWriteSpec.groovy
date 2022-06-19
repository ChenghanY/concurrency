package com.james.concurrency.all

import com.james.concurrency.ConcurrencyApplication
import com.james.concurrency.dataobject.Invoice
import com.james.concurrency.dataobject.ProductBuyer
import com.james.concurrency.mapper.InvoiceMapper
import com.james.concurrency.mapper.ProductBuyerMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 *  业务逻辑：对productId为1的产品抢购。
 *  1. 对`product_buyer`表的商品登记购买人
 *  2. 对`invoice` 发票表登记接收人 （通过product_buyer_id关联，同时冗余了buyer字段）
 *  note: 为了触发脏写，该业务底表没有用遵守三范式。
 *
 */
@SpringBootTest(classes = ConcurrencyApplication.class)
class DirtyWriteSpec extends Specification {

    @Autowired
    ProductBuyerMapper productBuyerMapper;

    @Autowired
    InvoiceMapper invoiceMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    def "任意隔离级别下，都解决了脏写问题"() {
        given:
        def productId = 1L;
        productBuyerMapper.updateBuyerByProductId("", productId);
        invoiceMapper.updateRecipientByProductBuyerId("", productId);

        // 按照《数据密集型应用系统设计》认为制造脏写
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    // 1. 购买人为james
                    productBuyerMapper.updateBuyerByProductId("james", productId);
                    Thread.sleep(5000);
                    // 4. 发票接收人为james
                    invoiceMapper.updateRecipientByProductBuyerId("james", productId);
                }
            });
        });

        // 线程(事务)B
        executorService.execute(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Thread.sleep(1000);
                    // 2. 购买人为kobe
                    productBuyerMapper.updateBuyerByProductId("kobe", productId);
                    // 3. 发票接收人为kobe
                    invoiceMapper.updateRecipientByProductBuyerId("kobe", productId);
                }
            });
        });
        executorService.shutdown();
        while(! executorService.isTerminated());

        expect:
        /*
        代码逻辑强制触发脏写，但是购买人和发票接收人如果是一致的，即触发失败。则脏写问题被mysql解决了。

        MySQL官网:

          A locking read, an UPDATE, or a DELETE generally set record locks on every
          index record that is scanned in the processing of an SQL statement.

          https://dev.mysql.com/doc/refman/5.7/en/innodb-locks-set.html

          update操作会加锁，这也就是为什么脏写问题被mysql解决了。
         */
        ProductBuyer productBuyer = productBuyerMapper.selectByProductId(productId);
        Invoice invoice = invoiceMapper.selectByProductBuyerId(productBuyer.getId());
        productBuyer.getBuyer() == invoice.getRecipient();
    }

}
