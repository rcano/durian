## Small libary for off-the-heap data representation

It intends to serve as base for other libraries or frameworks that do FFI or similar, on the JVM or JS or native.

### Description

This libary uses minimal concepts to lay out flat structures over flat memory regions:

* `MemorySegment`: similar to JFM's MemorySegment, a simplification of it, duplicated for cross platform support.
* `Address`: a memory pointer in some memory region
* `Pointer[Struct, Allocator]`: A pointer that is path dependently bound to an allocator. Ends up being aliased to just an `Address`
* `Size[T]`: typeclass providing size for `T`
* `Serde[T]`: A typeclass that knows how to read/write a type T from a memory segment.
* `StructLayout[Struct]`: a typeclass that extends Sized and knows how to map a field-index into a memory offset.

There are some auxiliary concepts but they are not relevant for understanding the libary, just plumbing to make things work.

### Example

```scala
// define a segment to work
val memorySegment: MemorySegment = JfmMemorySegment(java.lang.foreign.MemorySegment.ofArray(new Array[Byte](1024 * 1024 * 10)))

// instantiate our allocator over this region
val allocator = Allocator.Bump(memorySegment)

// define our struct type:
type Color = Struct {
  var alpha: Byte
  var red: Byte
  var green: Byte
  var blue: Byte
}
// and some necessary boilerplate. This boilerplate is always the exact same for every struct
object Color {
  val mirror = Struct.structMirror[Color]
  given mirror.type = mirror
  val layout = CompactLayouter.compactLayout[Color](using mirror)
  given layout.type = layout

  // custom initializer
  def apply(red: Byte = 0, green: Byte = 0, blue: Byte = 0, alpha: Byte = 0)(using alloc: Allocator): Pointer[Color, alloc.type] = {
    val res = alloc.allocStruct[Color]
    res.alpha = alpha
    res.red = red
    res.green = green
    res.blue = blue
    res
  }
}
// bring in the typeclasses into scope
import Color.given

//using here is a helper utility to turn a singleton type into a given. All operations on pointer require the tracked allocator to be in implicit scope.
using(allocator) {
  // within this block we can instantiate and use our struct
  val red = Color(red = 255.toByte)
  val green = Color(green = 255.toByte)

  assert(red.blue == green.blue)
  red.blue = 120
  // note that even if we were to return the struct outside this scope, the tracked allocator would prevent it from being used with the wrong memory region.
}

```
