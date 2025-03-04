package com.example

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val appMicrometerRegistry = SimpleMeterRegistry()

    install(MicrometerMetrics) { registry = appMicrometerRegistry }
    routing {
        get("/metrics-micrometer") {
            call.respond(appMicrometerRegistry.metersAsString)
        }
    }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
