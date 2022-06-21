package com.james.concurrency.mapper;

import com.james.concurrency.dataobject.BankAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BankAccountMapper {

    BankAccount selectById(@Param("id") Long id);

    BankAccount selectByIdForUpdate(@Param("id") Long id);

    void updateBalanceById(@Param("balance") int balance, @Param("id")Long id);

    void atomicUpdateBalanceByCostAndId(@Param("cost") int cost, @Param("id")Long id);

    BankAccount selectByIdLockInShareMode(@Param("id")Long id);

    List<BankAccount> selectListByBalanceGt(@Param("balance") int balance);

    List<BankAccount> selectListByBalanceGtForUpdate(@Param("balance") int balance);

    long insert(BankAccount bankAccount);

    void deleteByName(@Param("name")String name);
}
