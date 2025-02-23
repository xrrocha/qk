package pk

class PugTest extends munit.FunSuite:
  test("Expands Pug template correctly"):
    val template = "h1 Hello #{name}!"
    val name = "test.yml"
    val model = Map("name" -> "World")
    val result = Pug.render(template, name, model)
    assertEquals(
      result,
      "<h1>Hello World!</h1>"
    )
