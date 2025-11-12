package com.basebeta

import io.ktor.client.statement.*
import io.ktor.http.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.measureTime

class RetryWithBackoffTests {

   private lateinit var mockResponse: HttpResponse
   private lateinit var mockHeaders: Headers

   @BeforeEach
   fun setup() {
      mockResponse = mockk()
      mockHeaders = mockk()
      every { mockResponse.headers } returns mockHeaders
   }

   @Test
   fun `should return immediately on successful response`() = runBlocking {
      // Given
      every { mockResponse.status } returns HttpStatusCode.OK
      var callCount = 0

      // When
      val result = retryWithBackoff {
         callCount++
         mockResponse
      }

      // Then
      assertEquals(1, callCount, "Should only call the block once")
      assertEquals(mockResponse, result)
   }

   @Test
   fun `should retry on 429 status code`() = runBlocking {
      // Given
      var callCount = 0
      every { mockHeaders["Retry-After"] } returns null

      // When
      val result = retryWithBackoff(
         maxRetries = 3,
         initialDelayMs = 10
      ) {
         callCount++
         if (callCount <= 2) {
            every { mockResponse.status } returns HttpStatusCode.TooManyRequests
         } else {
            every { mockResponse.status } returns HttpStatusCode.OK
         }
         mockResponse
      }

      // Then
      assertEquals(3, callCount, "Should retry twice before succeeding")
      assertEquals(mockResponse, result)
   }

   @Test
   fun `should respect Retry-After header for 429 responses`() = runBlocking {
      // Given
      var callCount = 0
      every { mockHeaders["Retry-After"] } returns "1" // 1 second

      // When
      val duration = measureTime {
         retryWithBackoff(
            maxRetries = 2,
            initialDelayMs = 10
         ) {
            callCount++
            if (callCount == 1) {
               every { mockResponse.status } returns HttpStatusCode.TooManyRequests
            } else {
               every { mockResponse.status } returns HttpStatusCode.OK
            }
            mockResponse
         }
      }

      // Then
      assertEquals(2, callCount)
      assertTrue(duration.inWholeMilliseconds >= 1000, "Should wait at least 1 second due to Retry-After header")
   }

   @Test
   fun `should retry on 500 server errors`() = runBlocking {
      // Given
      var callCount = 0
      every { mockHeaders["Retry-After"] } returns null

      // When
      val result = retryWithBackoff(
         maxRetries = 3,
         initialDelayMs = 10
      ) {
         callCount++
         if (callCount <= 2) {
            every { mockResponse.status } returns HttpStatusCode.InternalServerError
         } else {
            every { mockResponse.status } returns HttpStatusCode.OK
         }
         mockResponse
      }

      // Then
      assertEquals(3, callCount, "Should retry on 500 errors")
      assertEquals(mockResponse, result)
   }

   @Test
   fun `should not retry on 400 client errors`() = runBlocking {
      // Given
      every { mockResponse.status } returns HttpStatusCode.BadRequest
      every { mockHeaders["Retry-After"] } returns null
      var callCount = 0

      // When
      val result = retryWithBackoff {
         callCount++
         mockResponse
      }

      // Then
      assertEquals(1, callCount, "Should not retry on 400 errors")
      assertEquals(HttpStatusCode.BadRequest, result.status)
   }

   @Test
   fun `should use exponential backoff for delays`() = runBlocking {
      // Given
      var callCount = 0
      every { mockHeaders["Retry-After"] } returns null
      every { mockResponse.status } returns HttpStatusCode.InternalServerError

      // When
      val duration = measureTime {
         retryWithBackoff(
            maxRetries = 3,
            initialDelayMs = 100,
            factor = 2.0
         ) {
            callCount++
            mockResponse
         }
      }

      // Then
      assertEquals(3, callCount)
      // Initial delay: 100ms, second delay: 200ms, total minimum: 300ms
      assertTrue(duration.inWholeMilliseconds >= 300, "Should use exponential backoff")
   }

   @Test
   fun `should respect max delay limit`() = runBlocking {
      // Given
      var callCount = 0
      every { mockHeaders["Retry-After"] } returns null
      every { mockResponse.status } returns HttpStatusCode.InternalServerError

      // When
      val duration = measureTime {
         retryWithBackoff(
            maxRetries = 5,
            initialDelayMs = 100,
            maxDelayMs = 200,
            factor = 10.0 // Would grow to 1000ms without cap
         ) {
            callCount++
            mockResponse
         }
      }

      // Then
      assertEquals(5, callCount)
      // With cap at 200ms: 100 + 200 + 200 + 200 = 700ms minimum
      assertTrue(duration.inWholeMilliseconds < 1000, "Should cap delays at maxDelayMs")
   }

   @Test
   fun `should exhaust retries and return last response`() = runBlocking {
      // Given
      every { mockResponse.status } returns HttpStatusCode.InternalServerError
      every { mockHeaders["Retry-After"] } returns null
      var callCount = 0

      // When
      val result = retryWithBackoff(
         maxRetries = 3,
         initialDelayMs = 10
      ) {
         callCount++
         mockResponse
      }

      // Then
      assertEquals(3, callCount, "Should exhaust all retries")
      assertEquals(HttpStatusCode.InternalServerError, result.status)
   }

   @Test
   fun `should retry on IOException`() = runBlocking {
      // Given
      var callCount = 0

      // When
      val result = retryWithBackoff(
         maxRetries = 3,
         initialDelayMs = 10
      ) {
         callCount++
         if (callCount <= 2) {
            throw IOException("Network error")
         } else {
            every { mockResponse.status } returns HttpStatusCode.OK
            mockResponse
         }
      }

      // Then
      assertEquals(3, callCount, "Should retry on IOException")
      assertEquals(mockResponse, result)
   }

   @Test
   fun `should retry on SocketTimeoutException`() = runBlocking {
      // Given
      var callCount = 0

      // When
      val result = retryWithBackoff(
         maxRetries = 3,
         initialDelayMs = 10
      ) {
         callCount++
         if (callCount == 1) {
            throw SocketTimeoutException("Timeout")
         } else {
            every { mockResponse.status } returns HttpStatusCode.OK
            mockResponse
         }
      }

      // Then
      assertEquals(2, callCount, "Should retry on SocketTimeoutException")
      assertEquals(mockResponse, result)
   }

   @Test
   fun `should retry on UnknownHostException`() = runBlocking {
      // Given
      var callCount = 0

      // When
      val result = retryWithBackoff(
         maxRetries = 3,
         initialDelayMs = 10
      ) {
         callCount++
         if (callCount == 1) {
            throw UnknownHostException("DNS failure")
         } else {
            every { mockResponse.status } returns HttpStatusCode.OK
            mockResponse
         }
      }

      // Then
      assertEquals(2, callCount, "Should retry on UnknownHostException")
      assertEquals(mockResponse, result)
   }

   @Test
   fun `should not retry on non-retryable exceptions`() = runBlocking {
      // Given
      var callCount = 0

      // When/Then
      assertThrows<IllegalArgumentException> {
         runBlocking {
            retryWithBackoff(
               maxRetries = 3,
               initialDelayMs = 10,
               retryOnException = { false } // Don't retry any exceptions
            ) {
               callCount++
               throw IllegalArgumentException("Invalid argument")
            }
         }
      }

      assertEquals(1, callCount, "Should not retry on non-retryable exception")
   }

   @Test
   fun `should throw exception after exhausting retries`() = runBlocking {
      // Given
      var callCount = 0

      // When/Then
      assertThrows<IOException> {
         runBlocking {
            retryWithBackoff(
               maxRetries = 3,
               initialDelayMs = 10
            ) {
               callCount++
               throw IOException("Persistent network error")
            }
         }
      }

      assertEquals(3, callCount, "Should exhaust all retries before throwing")
   }

   @Test
   fun `should use custom retry predicate for status codes`() = runBlocking {
      // Given
      every { mockResponse.status } returns HttpStatusCode.BadGateway
      every { mockHeaders["Retry-After"] } returns null
      var callCount = 0

      // When
      val result = retryWithBackoff(
         maxRetries = 3,
         initialDelayMs = 10,
         retryOn = { response ->
            response.status.value == 502 // Only retry on Bad Gateway
         }
      ) {
         callCount++
         if (callCount <= 2) {
            every { mockResponse.status } returns HttpStatusCode.BadGateway
         } else {
            every { mockResponse.status } returns HttpStatusCode.OK
         }
         mockResponse
      }

      // Then
      assertEquals(3, callCount, "Should use custom retry predicate")
      assertEquals(HttpStatusCode.OK, result.status)
   }

   @Test
   fun `should handle mixed failures - exceptions and HTTP errors`() = runBlocking {
      // Given
      var callCount = 0
      every { mockHeaders["Retry-After"] } returns null

      // When
      val result = retryWithBackoff(
         maxRetries = 4,
         initialDelayMs = 10
      ) {
         callCount++
         when (callCount) {
            1 -> throw IOException("Network error")
            2 -> {
               every { mockResponse.status } returns HttpStatusCode.InternalServerError
               mockResponse
            }
            3 -> throw SocketTimeoutException("Timeout")
            else -> {
               every { mockResponse.status } returns HttpStatusCode.OK
               mockResponse
            }
         }
      }

      // Then
      assertEquals(4, callCount, "Should handle mixed failure types")
      assertEquals(HttpStatusCode.OK, result.status)
   }
}