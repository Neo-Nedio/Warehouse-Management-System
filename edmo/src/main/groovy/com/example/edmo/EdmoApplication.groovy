package com.example.edmo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableAspectJAutoProxy
class EdmoApplication {

    static void main(String[] args) {
        SpringApplication.run(EdmoApplication, args)
    }

}
