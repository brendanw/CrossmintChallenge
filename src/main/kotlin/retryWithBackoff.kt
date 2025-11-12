package com.basebeta

import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun retryWithBackoff(
   maxRetries: Int = 5,
   initialDelayMs: Long = 500,
   maxDelayMs: Long = 30_000,
   factor: Double = 2.0,
   retryOn: (HttpResponse) -> Boolean = { response ->
      response.status.value == 429 || response.status.value >= 500
   },
   retryOnException: (Exception) -> Boolean = { exception ->
      // Retry on network-related exceptions
      when (exception) {
         is IOException,
         is SocketTimeoutException,
         is UnknownHostException -> true
         else -> false
      }
   },
   block: suspend () -> HttpResponse
): HttpResponse {
   var currentDelay = initialDelayMs
   var lastResponse: HttpResponse? = null
   var lastException: Exception? = null

   repeat(maxRetries) { attempt ->
      try {
         val response = block()
         lastResponse = response
         lastException = null

         if (response.status.isSuccess()) {
            return response
         }

         if (!retryOn(response)) {
            println("Request failed with status ${response.status.value}, not retrying")
            return response
         }

         // Handle rate limiting with Retry-After header
         val delayTime = if (response.status.value == 429) {
            val retryAfter = response.headers["Retry-After"]?.toLongOrNull()
            if (retryAfter != null) {
               retryAfter * 1000 // Convert seconds to milliseconds
            } else {
               currentDelay
            }
         } else {
            currentDelay
         }

         println("Request failed with status ${response.status.value}, retrying in ${delayTime}ms (attempt ${attempt + 1}/$maxRetries)")
         delay(delayTime)

      } catch (e: Exception) {
         lastException = e
         lastResponse = null

         // Check if we should retry this exception
         if (!retryOnException(e)) {
            println("Request failed with non-retryable exception: ${e::class.simpleName} - ${e.message}")
            throw e
         }

         // Don't retry on the last attempt
         if (attempt == maxRetries - 1) {
            println("Request failed after $maxRetries attempts with exception: ${e::class.simpleName} - ${e.message}")
            throw e
         }

         println("Request failed with exception: ${e::class.simpleName} - ${e.message}, retrying in ${currentDelay}ms (attempt ${attempt + 1}/$maxRetries)")
         delay(currentDelay)
      }

      // Exponential backoff for next attempt
      currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
   }

   // If we got here, we exhausted all retries
   return lastResponse
      ?: throw lastException
         ?: IllegalStateException("Exhausted $maxRetries retries without response or exception")
}