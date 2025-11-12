package com.basebeta

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() {
   runBlocking {
      // Fetch the goal grid
      val gridResponse = Deps.client.get("${Deps.baseUrl}/api/map/${Deps.candidateId}/goal") {
      }.body<GridResponse>()

      // Convert grid to list of space items
      val spaceItems = parseGridToSpaceItems(gridResponse.goal)

      println("Total items to create: ${spaceItems.size}")
      println("Breaking down by type:")
      println("  Polyanets: ${spaceItems.filterIsInstance<SpaceItem.Polyanet>().size}")
      println("  Soloons: ${spaceItems.filterIsInstance<SpaceItem.Soloon>().size}")
      println("  Comeths: ${spaceItems.filterIsInstance<SpaceItem.Cometh>().size}")
      println()

      var successCount = 0
      var failureCount = 0

      // Process all space items
      spaceItems.forEachIndexed { index, item ->
         val response = retryWithBackoff {
            Deps.client.post(urlString = "${Deps.baseUrl}${item.toApiPath()}") {
               contentType(ContentType.Application.Json)
               setBody(item.toApiRequestBody(Deps.candidateId))
            }
         }

         if (response.status.isSuccess()) {
            successCount++
            println("✓ Created ${item.getDescription()} at (${item.position.x}, ${item.position.y}) - Progress: ${index + 1}/${spaceItems.size}")
         } else {
            failureCount++
            println("✗ Failed to create ${item.getDescription()} at (${item.position.x}, ${item.position.y}): ${response.status}")
         }

         delay(100)
      }

      println("\n=== Summary ===")
      println("Total items processed: ${successCount + failureCount}")
      println("Successful: $successCount")
      println("Failed: $failureCount")
   }
}

fun parseGridToSpaceItems(grid: List<List<String>>): List<SpaceItem> {
   val items = mutableListOf<SpaceItem>()

   grid.forEachIndexed { rowIndex, row ->
      row.forEachIndexed { colIndex, cell ->
         val position = Coordinate(x = colIndex, y = rowIndex)

         when {
            cell == "POLYANET" -> {
               items.add(SpaceItem.Polyanet(position))
            }
            cell.endsWith("_SOLOON") -> {
               val color = cell.substringBefore("_SOLOON").lowercase()
               items.add(SpaceItem.Soloon(position, color))
            }
            cell.endsWith("_COMETH") -> {
               val direction = cell.substringBefore("_COMETH").lowercase()
               items.add(SpaceItem.Cometh(position, direction))
            }
            cell == "SPACE" -> {
               // Skip spaces
            }
            else -> {
               println("Warning: Unknown cell type: $cell at position $position")
            }
         }
      }
   }

   return items
}