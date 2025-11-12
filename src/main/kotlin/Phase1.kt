package com.basebeta

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


fun main() {
   // initialize HttpClient
   val client = HttpClient {
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

   val polyanetsToCreate: List<Coordinate> = getPolyanetsToCreateAsXShape()

   runBlocking {
      polyanetsToCreate.forEach { coordinate ->
         println("POST: $coordinate")
         val response = client.post(urlString = "${Deps.baseUrl}/api/polyanets") {
            contentType(ContentType.Application.Json)
            setBody(
               CreateSpaceItemRequest(
                  candidateId = Deps.candidateId,
                  row = coordinate.y,
                  column = coordinate.x
               )
            )
         }

         delay(50L)

         println("isSuccess = ${response.status.isSuccess()} statusCode=${response.status.value}")
      }
   }
}

fun getPolyanetsToCreateAsXShape(
   spaceSize: Int = 11,
   margin: Int = 2
): List<Coordinate> {
   val coordinates = mutableListOf<Coordinate>()

   val start = margin
   val end = spaceSize - margin - 1

   for (i in 0..(end - start)) {
      // Main diagonal
      coordinates.add(Coordinate(x = start + i, y = start + i))
      // Anti-diagonal
      coordinates.add(Coordinate(x = end - i, y = start + i))
   }

   return coordinates.distinct()
}

@Serializable
data class CreateSpaceItemRequest(
   val candidateId: String,
   val row: Int,
   val column: Int
)

data class Coordinate(val x: Int, val y: Int)
