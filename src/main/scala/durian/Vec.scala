package durian

import language.experimental.modularity
import compiletime.ops.int.*

opaque type Vec[S, N <: Int & Singleton] = Address
object Vec {
  given [S: Sized, N <: Int & Singleton: ValueOf] => Sized[Vec[S, N]] = Sized(Sized.of[S].size * valueOf[N])

  def apply[S](n: Int & Singleton)(using alloc: Allocator, sized: Sized[Vec[S, n.type]]): Pointer[Vec[S, n.type], alloc.memorySegment.type] =
    alloc.allocPtr[Vec[S, n.type]]

  extension [S <: Struct, N <: Int & Singleton, Mem: Precise](v: Pointer[Vec[S, N], Mem]) {
    def at(i: Int)(using (i.type < N) =:= true)(using structSize: Sized[S]): Pointer[S, Mem] =
      Pointer.unsafe(v.pointerAddress + Address.unsafe((structSize.size * i)))
  }
}
