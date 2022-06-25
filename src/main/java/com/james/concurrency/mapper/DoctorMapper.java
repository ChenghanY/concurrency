package com.james.concurrency.mapper;

import com.james.concurrency.dataobject.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DoctorMapper {

    Long countByShiftIdAndOnCall(@Param("shiftId") Long shiftId, @Param("onCall") Boolean onCall);

    void updateOnCallByShiftIdAndName(@Param("onCall") Boolean onCall, @Param("shiftId") Long shiftId, @Param("name") String name);

    void insert(Doctor doctor);

    void deleteByName(@Param("name") String name);
}
