package com.james.concurrency;

import com.james.concurrency.dataobject.City;
import com.james.concurrency.mapper.CityMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConcurrencyApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ConcurrencyApplication.class, args);
	}

	private final CityMapper cityMapper;

	public ConcurrencyApplication(CityMapper cityMapper) {
		this.cityMapper = cityMapper;
	}

	@Override
	public void run(String... args) throws Exception {
		City ca = this.cityMapper.findByState("CA");
		System.out.println(ca);
		System.out.println(ca.getId());
	}
}
