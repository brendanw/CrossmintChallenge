import com.basebeta.Coordinate
import com.basebeta.getPolyanetsToCreateAsXShape
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class Phase1HappyPathTest {

   @Test
   fun `Generate Phase1 Goal Map`() {
      val coordinates = getPolyanetsToCreateAsXShape()

      val goalGrid = Json.decodeFromString<List<List<String>>>(goal)

      val expectedCoordinates = extractCoordinatesFromGoal(goalGrid, "POLYANET")

      val generatedGrid = createSpaceGridWithCoordinates(coordinates, "POLYANET")

      assertEquals(expectedCoordinates.toSet(), coordinates.toSet(),
         "Generated coordinates should match goal POLYANET positions")

      assertEquals(goalGrid, generatedGrid,
         "Generated grid should match goal grid")
   }

   @Test
   fun `extract coordinates from simpleGoal`() {
      val goalGrid = Json.decodeFromString<List<List<String>>>(simpleGoal)

      val coordinates = extractCoordinatesFromGoal(goalGrid, "POLYANET")

      val expectedCoordinates = listOf(
         Coordinate(0, 0),
         Coordinate(1, 1),
         Coordinate(2, 2)
      )

      assertEquals(expectedCoordinates, coordinates,
         "Generated coordinates should match coordinates")
   }

   private fun extractCoordinatesFromGoal(
      grid: List<List<String>>,
      itemType: String
   ): List<Coordinate> {
      val coordinates = mutableListOf<Coordinate>()

      for (row in grid.indices) {
         for (col in grid[row].indices) {
            if (grid[row][col] == itemType) {
               coordinates.add(Coordinate(x = col, y = row))
            }
         }
      }

      return coordinates
   }

   fun createSpaceGridWithCoordinates(
      coordinates: List<Coordinate>,
      itemType: String
   ): List<List<String>> {
      // Create an 11x11 grid filled with "SPACE"
      val grid = MutableList(11) { MutableList(11) { "SPACE" } }

      // Place the itemType at each coordinate
      coordinates.forEach { coordinate ->
         grid[coordinate.y][coordinate.x] = itemType
      }

      return grid
   }
}

val simpleGoal = """
   [
      [
        "POLYANET",
        "SPACE",
        "SPACE"
      ],
      [
        "SPACE",
        "POLYANET",
        "SPACE"
      ],
      [
        "SPACE",
        "SPACE",
        "POLYANET"
      ]
   ]
""".trimIndent()

val goal = """
      [
        [
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE"
        ],
        [
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE"
        ],
        [
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE"
        ],
        [
            "SPACE",
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE",
            "SPACE"
        ],
        [
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE"
        ],
        [
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE"
        ],
        [
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE"
        ],
        [
            "SPACE",
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE",
            "SPACE"
        ],
        [
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "POLYANET",
            "SPACE",
            "SPACE"
        ],
        [
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE"
        ],
        [
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE",
            "SPACE"
        ]
    ]
   """.trimIndent()