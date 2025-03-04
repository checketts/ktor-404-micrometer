package com.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `test Unique 404s Create Unbounded Metrics Growth`() = runTest {
        testApplication {
            val appMicrometerRegistry = SimpleMeterRegistry()

            install(MicrometerMetrics) { registry = appMicrometerRegistry }
            routing {
                get("/metrics-micrometer") {
                    call.respond(appMicrometerRegistry.metersAsString)
                }
            }

            (1..1000).forEach {
                (1..10_000).map {
                    client.get("/${UUID.randomUUID()}")
                }
                println("Metrics page size: ${client.get("/metrics-micrometer").bodyAsText().length}")
            }

        }
    }

    class Filter404: MeterFilter {
        override fun map(id: Meter.Id): Meter.Id {
            if(id.name == "ktor.http.server.requests" && id.getTag("status") == "404") {
                return id.withTag(Tag.of("route", "404"));
            }
            return id;
        }
    }

    @Test
    fun `test Unique 404s filter to avoid memory growth`() = runTest {
        testApplication {
            val appMicrometerRegistry = SimpleMeterRegistry().apply {
                config().meterFilter(Filter404())
            }

            install(MicrometerMetrics) { registry = appMicrometerRegistry }
            routing {
                get("/metrics-micrometer") {
                    call.respond(appMicrometerRegistry.metersAsString)
                }
            }

            (1..1000).forEach {
                (1..10_000).map {
                    client.get("/${UUID.randomUUID()}")
                }
                println("Metrics page size: ${client.get("/metrics-micrometer").bodyAsText().length}")
            }

        }
    }

}
