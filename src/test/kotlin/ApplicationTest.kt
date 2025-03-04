package com.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
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
            application {
                module()
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
