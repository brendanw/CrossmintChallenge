package com.basebeta

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable


fun main() {
   val polyanetsToCreate: List<Coordinate> = getPolyanetsToCreateAsXShape()

   runBlocking {
      polyanetsToCreate.forEach { coordinate ->
         println("POST: $coordinate")
         val response = Deps.client.post(urlString = "${Deps.baseUrl}/api/polyanets") {
            contentType(ContentType.Application.Json)
            setBody(
               CreatePolyanetRequest(
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
data class CreatePolyanetRequest(
   val candidateId: String,
   val row: Int,
   val column: Int
)

data class Coordinate(val x: Int, val y: Int)
