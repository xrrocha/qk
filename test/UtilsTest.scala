package qk

import Utils.*
import Database.executeQuery
import scala.util.{Failure, Success, Try, Using}

class UtilsTest extends munit.FunSuite:
  test("Normalizes space"):
    assertEquals(
      normalizeSpace("\tsome     wildly \t different    spacing"),
      normalizeSpace("some wildly different spacing")
    )
end UtilsTest
