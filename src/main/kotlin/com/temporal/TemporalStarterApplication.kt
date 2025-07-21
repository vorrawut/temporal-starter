package com.temporal

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TemporalStarterApplication

fun main(args: Array<String>) {
	runApplication<TemporalStarterApplication>(*args)
}
