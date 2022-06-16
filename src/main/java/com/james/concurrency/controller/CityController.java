package com.james.concurrency.controller;

import com.james.concurrency.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CityController {

    @Autowired
    CityService cityService;

    @GetMapping("/city")
    public String city() {
        cityService.update();
        return "success";
    }
}
