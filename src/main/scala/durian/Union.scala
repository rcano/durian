package durian

import language.experimental.modularity

opaque type Union[Tup <: Tuple] = Address
object Union {
  inline given [Tup <: Tuple: Sized.Max]: Sized[Union[Tup]] = Sized.of[Tup].asInstanceOf

  def unsafe[Tup <: Tuple: Sized.Max, Mem](a: Address): Pointer[Union[Tup], Mem] = Pointer.unsafe(a)

  extension [Tup <: Tuple, Mem: Precise](u: Pointer[Union[Tup], Mem]) {
    def as[E: Sized](using Tuple.Contains[Tup, E] =:= true): Pointer[E, Mem] = u.asInstanceOf
  }
}
