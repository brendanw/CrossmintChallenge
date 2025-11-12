import com.basebeta.CoordinateListHolder
import com.basebeta.SpaceItem
import com.basebeta.convertGridToCoordinateList
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class Phase2Tests {
   @Test
   fun testParsingSimpleGoal() {
      // Parse the JSON string into a List<List<String>>
      val goalGrid = Json.decodeFromString<List<List<String>>>(simpleGoal)

      // Convert to coordinate lists
      val result = convertGridToCoordinateList(goalGrid)

      // Expected Polyanets
      val expectedPolyanets = listOf(
         SpaceItem.Polyanet(x = 0, y = 0),
         SpaceItem.Polyanet(x = 4, y = 2)
      )

      // Expected Soloons
      val expectedSoloons = listOf(
         SpaceItem.Soloon(x = 2, y = 0, color = "blue"),
         SpaceItem.Soloon(x = 1, y = 1, color = "red"),
         SpaceItem.Soloon(x = 3, y = 1, color = "purple"),
         SpaceItem.Soloon(x = 0, y = 3, color = "white")
      )

      // Expected Comeths
      val expectedComeths = listOf(
         SpaceItem.Cometh(x = 3, y = 0, direction = "up"),
         SpaceItem.Cometh(x = 0, y = 2, direction = "down"),
         SpaceItem.Cometh(x = 2, y = 2, direction = "right"),
         SpaceItem.Cometh(x = 3, y = 2, direction = "left"),
         SpaceItem.Cometh(x = 2, y = 3, direction = "up")
      )

      // Assertions
      assertEquals(
         expectedPolyanets.toSet(),
         result.polyanetList.toSet(),
         "Polyanets should match expected coordinates"
      )

      assertEquals(
         expectedSoloons.toSet(),
         result.soloonList.toSet(),
         "Soloons should match expected coordinates and colors"
      )

      assertEquals(
         expectedComeths.toSet(),
         result.comethList.toSet(),
         "Comeths should match expected coordinates and directions"
      )

      // Also verify counts
      assertEquals(2, result.polyanetList.size, "Should have 2 Polyanets")
      assertEquals(4, result.soloonList.size, "Should have 4 Soloons")
      assertEquals(5, result.comethList.size, "Should have 5 Comeths")
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