package durian

import language.experimental.modularity
import language.experimental.erasedDefinitions

class BasicTests extends munit.FunSuite {
  val memorySegment: MemorySegment = JfmMemorySegment(java.lang.foreign.MemorySegment.ofArray(new Array[Byte](1024 * 1024 * 10)))

  type Color = Struct { var alpha: Byte; var red: Byte; var green: Byte; var blue: Byte }
  object Color {
    val mirror = Struct.structMirror[Color]
    given mirror.type = mirror
    val layout = CompactLayouter.compactLayout[Color](using mirror)
    given layout.type = layout
    def apply(red: Byte = 0, green: Byte = 0, blue: Byte = 0, alpha: Byte = 0)(using alloc: Allocator): Pointer[Color, alloc.type] = {
      val res = alloc.allocStruct[Color]
      res.alpha = alpha
      res.red = red
      res.green = green
      res.blue = blue
      res
    }
  }
  import Color.given

  type Health = Struct { var value: Double }
  object Health {
    val mirror = Struct.structMirror[Health]
    given mirror.type = mirror
    val layout = CompactLayouter.compactLayout[Health](using mirror)
    given layout.type = layout
  }
  import Health.given

  type Positioned = Struct { var x: Float; var y: Float }
  object Positioned {
    val mirror = Struct.structMirror[Positioned]
    given mirror.type = mirror
    val layout = CompactLayouter.compactLayout[Positioned](using mirror)
    given layout.type = layout
  }
  import Positioned.given

  type Character = Struct {
    val health: Inlined[Health]
    val position: Inlined[Positioned]
    val color: Inlined[Color]
  }
  object Character {
    val mirror = Struct.structMirror[Character]
    given mirror.type = mirror
    val layout = CompactLayouter.compactLayout[Character](using mirror)
    given layout.type = layout
  }
  import Character.given

  test("can allocate based on size") {
    using(Allocator.Bump(memorySegment)) {
      val c = Color()
    }
  }

  test("can read and write") {
    using(Allocator.Bump(memorySegment)) {
      val c = Color()
      c.red = 10
      c.green = 240.toByte
      c.blue = 100

      assertEquals(c.red, 10.toByte)
      assertEquals(c.green, 240.toByte)
      assertEquals(c.blue, 100.toByte)
    }
  }

  test("can read and write Inlined") {
    using(Allocator.Bump(memorySegment)) {
      val c = summon[Allocator].allocStruct[Character]
      c.position.x = 10
      c.position.y = 15
      c.health.value = 100.4

      assertEquals(c.position.x, 10f)
      assertEquals(c.position.y, 15f)
      assertEquals(c.health.value, 100.4)
    }
  }

}
