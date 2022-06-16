package com.james.concurrency.service.impl;

import com.james.concurrency.mapper.CityMapper;
import com.james.concurrency.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CityServiceImpl implements CityService {

    @Autowired
    CityMapper cityMapper;

    @Override
    @Transactional
    public void update() {
        cityMapper.updateStateById("CA", 1L);
    }
}
