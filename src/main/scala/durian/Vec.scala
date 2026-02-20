package durian

import compiletime.ops.int.*

opaque type Vec[S <: Struct, N <: Int & Singleton] <: Struct = Struct
object Vec {
  given [S <: Struct: Sized, N <: Int & Singleton: ValueOf] => Sized[Vec[S, N]] = Sized(Sized.of[S].size * valueOf[N])

  def apply[S <: Struct](n: Int & Singleton)(using alloc: Allocator, sized: Sized[Vec[S, n.type]]): Pointer[Vec[S, n.type], alloc.type] =
    alloc.allocStruct[Vec[S, n.type]]

  extension [S <: Struct, N <: Int & Singleton, Alloc <: Allocator](v: Pointer[Vec[S, N], Alloc]) {
    def apply(i: Int)(using (i.type < N) =:= true)(using structSize: Sized[S], alloc: Alloc): Pointer[S, Alloc] =
      Pointer(v.memAddress + (structSize.size * i))
  }
}
