package com.james.concurrency.mapper;

import com.james.concurrency.dataobject.Invoice;
import com.james.concurrency.dataobject.ProductBuyer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InvoiceMapper {

    Invoice selectByProductBuyerId(@Param("productBuyerId")Long productBuyerId);

    void updateRecipientByProductBuyerId(@Param("recipient") String recipient, @Param("productBuyerId")Long productBuyerId);

}
