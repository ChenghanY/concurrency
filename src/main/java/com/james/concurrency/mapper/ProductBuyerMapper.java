package com.james.concurrency.mapper;

import com.james.concurrency.dataobject.ProductBuyer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductBuyerMapper {

    void updateBuyerByProductId(@Param("buyer") String buyer, @Param("productId")Long productId);

    ProductBuyer selectByProductId(@Param("productId")Long productId);

}
