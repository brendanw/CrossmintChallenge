package com.basebeta

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

fun main() {
   runBlocking {
      val gridResponse = Deps.client.get("${Deps.baseUrl}/api/map/${Deps.candidateId}/goal") {

      }.body<GridResponse>()

      println("goalGrid.size: ${gridResponse.goal.size}")

      val listHolder = convertGridToCoordinateList(gridResponse.goal)

      listHolder.polyanetList.forEach { item ->
         val response = Deps.client.post(urlString = "${Deps.baseUrl}/api/polyanets") {
            contentType(ContentType.Application.Json)
            setBody(
               CreatePolyanetRequest(
                  candidateId = Deps.candidateId,
                  row = item.y,
                  column = item.x
               )
            )
         }

         delay(50)
      }

      listHolder.soloonList.forEach { item ->
         val response = Deps.client.post(urlString = "${Deps.baseUrl}/api/soloons") {
            contentType(ContentType.Application.Json)
            setBody(
               CreateSoloonRequest(
                  candidateId = Deps.candidateId,
                  row = item.y,
                  column = item.x,
                  color = item.color
               )
            )
         }

         delay(50)
      }

      listHolder.comethList.forEach { item ->
         val response = Deps.client.post(urlString = "${Deps.baseUrl}/api/comeths") {
            contentType(ContentType.Application.Json)
            setBody(
               CreateComethRequest(
                  candidateId = Deps.candidateId,
                  row = item.y,
                  column = item.x,
                  direction = item.direction
               )
            )
         }

         delay(50)
      }
   }
}

fun convertGridToCoordinateList(grid: List<List<String>>): CoordinateListHolder {
   return CoordinateListHolder(emptyList(), emptyList(), emptyList())
}

@Serializable
data class GridResponse(val goal: List<List<String>>)

data class CoordinateListHolder(
   val polyanetList: List<SpaceItem.Polyanet>,
   val comethList: List<SpaceItem.Cometh>,
   val soloonList: List<SpaceItem.Soloon>
)

sealed class SpaceItem {
   data class Polyanet(val x: Int, val y: Int): SpaceItem()
   data class Soloon(val x: Int, val y: Int, val color: String): SpaceItem()
   data class Cometh(val x: Int, val y: Int, val direction: String): SpaceItem()
}

@Serializable
data class CreateSoloonRequest(
   val candidateId: String,
   val row: Int,
   val column: Int,
   val color: String
)

@Serializable
data class CreateComethRequest(
   val candidateId: String,
   val row: Int,
   val column: Int,
   val direction: String
)