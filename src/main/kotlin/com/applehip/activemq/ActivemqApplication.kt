package com.applehip.activemq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing //Jpa Auditing을 활성화 시키기 위한 어노테이션
@SpringBootApplication
class ActivemqApplication

fun main(args: Array<String>) {
    runApplication<ActivemqApplication>(*args)
}
