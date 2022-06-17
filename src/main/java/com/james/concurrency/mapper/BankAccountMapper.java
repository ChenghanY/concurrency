package com.james.concurrency.mapper;

import com.james.concurrency.dataobject.BankAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BankAccountMapper {

    BankAccount selectById(@Param("id") Long id);

    void updateBalanceById(@Param("balance") int balance, @Param("id")Long id);

}
