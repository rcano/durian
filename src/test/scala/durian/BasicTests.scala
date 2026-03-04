package durian

import java.lang.foreign as jfm

class BasicTests extends munit.FunSuite {
  def memorySegment: jfm.MemorySegment = java.lang.foreign.MemorySegment.ofArray(new Array[Byte](128))

  test("Struct sizing") {
    case class Point(x: Int, y: Int) extends Struct derives CompactLayout

    assert(summon[Sized[Point]].size == 8)
  }

  test("pointer serialization") {
    val mem: jfm.MemorySegment = memorySegment
    val p: Pointer[Int, mem.type] = Pointer.unsafe(Address.unsafe(0))
    p()
    p := 23
  }

  test("pointers to structs") {
    case class Point(x: Int, y: Int) extends Struct derives CompactLayout
    val mem: jfm.MemorySegment = memorySegment
    val p: Pointer[Point, mem.type] = Pointer.unsafe(Address.unsafe(0))

    val deref = p.→.x
  }

  test("pointers to nested structs") {
    case class Point(x: Int, y: Int) extends Struct derives CompactLayout
    case class Rectangle(topLeft: Point, botRight: Point) extends Struct derives CompactLayout
    val mem: jfm.MemorySegment = memorySegment
    val p: Pointer[Rectangle, mem.type] = Pointer.unsafe(Address.unsafe(0))

    val deref = p.→.topLeft.→.y()
    println(s"got deref $deref")
    p.→.botRight.→.x := 84
  }

  test("structs with pointers to structs") {
    case class Point(x: Int, y: Int) extends Struct derives CompactLayout
    case class Rectangle(topLeft: NestedPointer[Point], botRight: NestedPointer[Point]) extends Struct derives CompactLayout
    val mem: jfm.MemorySegment = memorySegment
    val r: Pointer[Rectangle, mem.type] = Pointer.unsafe(Address.unsafe(0))
    val p: Pointer[Point, mem.type] = Pointer.unsafe(Address.unsafe(16))
    val xp = r.→.botRight := p
    r.→.topLeft().→.y := 15
  }

  case class Color private (red: Byte, green: Byte, blue: Byte) extends Struct derives CompactLayout
  object Color {
    def apply(r: Byte, g: Byte, b: Byte)(using a: Allocator): Pointer[Color, a.memorySegment.type] = {
      val res = a.allocPtr[Color]
      res.→.red := r
      res.→.green := g
      res.→.blue := b
      res
    }
  }

  test("bump allocator direct") {
    val alloc = Allocator.Bump(memorySegment)

    val c = alloc.allocPtr[Color]
    c.→.red := 10
    c.→.green := 240.toByte
    c.→.blue := 100

    assertEquals(c.→.red(), 10.toByte)
    assertEquals(c.→.green(), 240.toByte)
    assertEquals(c.→.blue(), 100.toByte)
  }

  test("bump allocator indirect") {
    using(Allocator.Bump(memorySegment)) { alloc ?=>
      val c = Color(1.toByte, 2.toByte, 3.toByte)
      c.→.red := 10
      c.→.green := 240.toByte
      c.→.blue := 100

      assertEquals(c.→.red(), 10.toByte)
      assertEquals(c.→.green(), 240.toByte)
      assertEquals(c.→.blue(), 100.toByte)
    }
  }

  test("JFM arenas interop") {
    val arena = Arena(java.lang.foreign.Arena.ofAuto())

    val c = arena.allocStruct[Color]
    c.value.→.red := 10
    c.value.→.green := 240.toByte
    c.value.→.blue := 100

    assertEquals(c.value.→.red(), 10.toByte)
    assertEquals(c.value.→.green(), 240.toByte)
    assertEquals(c.value.→.blue(), 100.toByte)
  }

  test("vectors") {
    val alloc = Allocator.Bump(memorySegment)

    val c = Vec[Color](10)(using alloc)
    c.at(0).→.red := 10
    c.at(1).→.red := 15
    assertEquals(c.at(0).→.red(), 10.toByte)
    assertEquals(c.at(1).→.red(), 15.toByte)
    assertEquals(c.at(2).→.red(), 0.toByte)
  }
}
