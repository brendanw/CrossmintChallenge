package com.basebeta

sealed class SpaceItem {
   abstract val position: Coordinate

   data class Polyanet(override val position: Coordinate) : SpaceItem()
   data class Soloon(override val position: Coordinate, val color: String) : SpaceItem()
   data class Cometh(override val position: Coordinate, val direction: String) : SpaceItem()
}

// Extension functions for API operations
fun SpaceItem.toApiPath(): String = when (this) {
   is SpaceItem.Polyanet -> "/api/polyanets"
   is SpaceItem.Soloon -> "/api/soloons"
   is SpaceItem.Cometh -> "/api/comeths"
}

fun SpaceItem.toApiRequestBody(candidateId: String): Any = when (this) {
   is SpaceItem.Polyanet -> CreatePolyanetRequest(
      candidateId = candidateId,
      row = position.y,
      column = position.x
   )
   is SpaceItem.Soloon -> CreateSoloonRequest(
      candidateId = candidateId,
      row = position.y,
      column = position.x,
      color = color
   )
   is SpaceItem.Cometh -> CreateComethRequest(
      candidateId = candidateId,
      row = position.y,
      column = position.x,
      direction = direction
   )
}

fun SpaceItem.getDescription(): String = when (this) {
   is SpaceItem.Polyanet -> "Polyanet"
   is SpaceItem.Soloon -> "$color Soloon"
   is SpaceItem.Cometh -> "$direction Cometh"
}