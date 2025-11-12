package com.basebeta

import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay

suspend fun retryWithBackoff(
   maxRetries: Int = 5,
   initialDelayMs: Long = 500,
   maxDelayMs: Long = 30_000,
   factor: Double = 2.0,
   retryOn: (HttpResponse) -> Boolean = { response ->
      response.status.value == 429 || response.status.value >= 500
   },
   block: suspend () -> HttpResponse
): HttpResponse {
   var currentDelay = initialDelayMs
   var lastResponse: HttpResponse? = null

   repeat(maxRetries) { attempt ->
      val response = block()
      lastResponse = response

      if (response.status.isSuccess()) {
         return response
      }

      if (!retryOn(response)) {
         println("Request failed with status ${response.status.value}, not retrying")
         return response
      }

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

      currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
   }

   return lastResponse ?: throw IllegalStateException("No response received")
}