package com.basebeta

sealed class SpaceItem {
   data class Polyanet(val x: Int, val y: Int): SpaceItem()
   data class Soloon(val x: Int, val y: Int, val color: String): SpaceItem()
   data class Cometh(val x: Int, val y: Int, val direction: String): SpaceItem()
}