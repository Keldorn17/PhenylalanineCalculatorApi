package com.keldorn.phenylalaninecalculatorapi;

import org.springframework.boot.SpringApplication;

public class TestPhenylalanineCalculatorApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(PhenylalanineCalculatorApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
