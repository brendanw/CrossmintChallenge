package com.basebeta

import kotlinx.serialization.Serializable

@Serializable
data class CreatePolyanetRequest(
   val candidateId: String,
   val row: Int,
   val column: Int
)

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

@Serializable
data class GridResponse(val goal: List<List<String>>)