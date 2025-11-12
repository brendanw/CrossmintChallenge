package com.basebeta

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object Deps {
   const val baseUrl = "https://challenge.crossmint.io"

   const val candidateId = "8afaff15-c1fb-489a-8f1a-140c7da33538"

   val client: HttpClient by lazy {
      HttpClient {
         install(ContentNegotiation) {
            json(
               Json {
                  isLenient = true
                  prettyPrint = true
                  ignoreUnknownKeys = true
               }
            )
         }

         install(HttpRedirect) {
            checkHttpMethod = false // Allow redirects for POST requests
            allowHttpsDowngrade = false
         }

         followRedirects = true
      }
   }
}