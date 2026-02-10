package com.nexusfi.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NexusFiServerApplication

fun main(args: Array<String>) {
    runApplication<NexusFiServerApplication>(*args)
}
