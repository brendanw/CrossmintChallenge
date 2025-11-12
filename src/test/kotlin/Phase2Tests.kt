import com.basebeta.Coordinate
import com.basebeta.SpaceItem
import com.basebeta.parseGridToSpaceItems
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class Phase2Tests {
   @Test
   fun testParsingSimpleGoal() {
      // Parse the JSON string into a List<List<String>>
      val goalGrid = Json.decodeFromString<List<List<String>>>(simpleGoal)

      // Convert to space items list
      val items = parseGridToSpaceItems(goalGrid)

      // Expected items
      val expectedItems = listOf(
         SpaceItem.Polyanet(Coordinate(0, 0)),
         SpaceItem.Soloon(Coordinate(2, 0), "blue"),
         SpaceItem.Cometh(Coordinate(3, 0), "up"),
         SpaceItem.Soloon(Coordinate(1, 1), "red"),
         SpaceItem.Soloon(Coordinate(3, 1), "purple"),
         SpaceItem.Cometh(Coordinate(0, 2), "down"),
         SpaceItem.Cometh(Coordinate(2, 2), "right"),
         SpaceItem.Cometh(Coordinate(3, 2), "left"),
         SpaceItem.Polyanet(Coordinate(4, 2)),
         SpaceItem.Soloon(Coordinate(0, 3), "white"),
         SpaceItem.Cometh(Coordinate(2, 3), "up")
      )

      // Assertions
      assertEquals(
         expectedItems.toSet(),
         items.toSet(),
         "Parsed items should match expected items"
      )

      // Verify counts by type
      assertEquals(2, items.filterIsInstance<SpaceItem.Polyanet>().size, "Should have 2 Polyanets")
      assertEquals(4, items.filterIsInstance<SpaceItem.Soloon>().size, "Should have 4 Soloons")
      assertEquals(5, items.filterIsInstance<SpaceItem.Cometh>().size, "Should have 5 Comeths")
   }

   val simpleGoal = """
    [
        [
            "POLYANET",
            "SPACE",
            "BLUE_SOLOON",
            "UP_COMETH",
            "SPACE"
        ],
        [
            "SPACE",
            "RED_SOLOON",
            "SPACE",
            "PURPLE_SOLOON",
            "SPACE"
        ],
        [
            "DOWN_COMETH",
            "SPACE",
            "RIGHT_COMETH",
            "LEFT_COMETH",
            "POLYANET"
        ],
        [
            "WHITE_SOLOON",
            "SPACE",
            "UP_COMETH",
            "SPACE",
            "SPACE"
        ]
    ]
    """.trimIndent()
}