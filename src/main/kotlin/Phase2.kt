package com.basebeta

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() {
   runBlocking {
      val gridResponse = Deps.client.get("${Deps.baseUrl}/api/map/${Deps.candidateId}/goal") {
      }.body<GridResponse>()

      val listHolder = convertGridToCoordinateList(gridResponse.goal)

      var successCount = 0
      var failureCount = 0

      // Process Polyanets
      listHolder.polyanetList.forEachIndexed { index, item ->
         val response = retryWithBackoff {
            Deps.client.post(urlString = "${Deps.baseUrl}/api/polyanets") {
               contentType(ContentType.Application.Json)
               setBody(
                  CreatePolyanetRequest(
                     candidateId = Deps.candidateId,
                     row = item.y,
                     column = item.x
                  )
               )
            }
         }

         if (response.status.isSuccess()) {
            successCount++
            println("✓ Created Polyanet at (${item.x}, ${item.y}) - Progress: ${index + 1}/${listHolder.polyanetList.size}")
         } else {
            failureCount++
            println("✗ Failed to create Polyanet at (${item.x}, ${item.y}): ${response.status}")
         }

         // Base delay between different requests to avoid hammering the server
         delay(100)
      }

      // Similar for Soloons
      listHolder.soloonList.forEachIndexed { index, item ->
         val response = retryWithBackoff {
            Deps.client.post(urlString = "${Deps.baseUrl}/api/soloons") {
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
         }

         if (response.status.isSuccess()) {
            successCount++
            println("✓ Created ${item.color} Soloon at (${item.x}, ${item.y}) - Progress: ${index + 1}/${listHolder.soloonList.size}")
         } else {
            failureCount++
            println("✗ Failed to create Soloon at (${item.x}, ${item.y}): ${response.status}")
         }

         delay(100)
      }

      // Similar for Comeths
      listHolder.comethList.forEachIndexed { index, item ->
         val response = retryWithBackoff {
            Deps.client.post(urlString = "${Deps.baseUrl}/api/comeths") {
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
         }

         if (response.status.isSuccess()) {
            successCount++
            println("✓ Created ${item.direction} Cometh at (${item.x}, ${item.y}) - Progress: ${index + 1}/${listHolder.comethList.size}")
         } else {
            failureCount++
            println("✗ Failed to create Cometh at (${item.x}, ${item.y}): ${response.status}")
         }

         delay(100)
      }

      println("\n=== Summary ===")
      println("Total items processed: ${successCount + failureCount}")
      println("Successful: $successCount")
      println("Failed: $failureCount")
   }
}

fun convertGridToCoordinateList(grid: List<List<String>>): CoordinateListHolder {
   val polyanets = mutableListOf<SpaceItem.Polyanet>()
   val soloons = mutableListOf<SpaceItem.Soloon>()
   val comeths = mutableListOf<SpaceItem.Cometh>()

   grid.forEachIndexed { rowIndex, row ->
      row.forEachIndexed { colIndex, cell ->
         when {
            cell == "POLYANET" -> {
               polyanets.add(SpaceItem.Polyanet(x = colIndex, y = rowIndex))
            }
            cell.endsWith("_SOLOON") -> {
               val color = cell.substringBefore("_SOLOON").lowercase()
               soloons.add(SpaceItem.Soloon(
                  x = colIndex,
                  y = rowIndex,
                  color = color
               ))
            }
            cell.endsWith("_COMETH") -> {
               val direction = cell.substringBefore("_COMETH").lowercase()
               comeths.add(SpaceItem.Cometh(
                  x = colIndex,
                  y = rowIndex,
                  direction = direction
               ))
            }
            cell == "SPACE" -> {
               // Skip spaces
            }
            else -> {
               println("Warning: Unknown cell type: $cell at ($colIndex, $rowIndex)")
            }
         }
      }
   }

   println("Found: ${polyanets.size} Polyanets, ${soloons.size} Soloons, ${comeths.size} Comeths")

   return CoordinateListHolder(
      polyanetList = polyanets,
      soloonList = soloons,
      comethList = comeths
   )
}

data class CoordinateListHolder(
   val polyanetList: List<SpaceItem.Polyanet>,
   val comethList: List<SpaceItem.Cometh>,
   val soloonList: List<SpaceItem.Soloon>
)