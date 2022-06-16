package com.james.concurrency.mapper;

import com.james.concurrency.dataobject.City;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CityMapper {

    @Select("SELECT * FROM CITY WHERE state = #{state}")
    City findByState(@Param("state") String state);

    @Update("UPDATE CITY set state = #{state} where id = #{id}")
    void updateStateById(@Param("state") String state, @Param("id") Long id);

}