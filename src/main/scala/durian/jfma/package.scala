package durian

import java.lang.foreign.ValueLayout

/** Special compatibility types for working with java foreign memory and access layer
  */
package object jfma {

  /** A special pointer backed directly by a JFMA MemorySegment */
  opaque type JfmaPointer[T] <: java.lang.foreign.MemorySegment = java.lang.foreign.MemorySegment

  object JfmaPointer {

    def unsafe[T](m: java.lang.foreign.MemorySegment): JfmaPointer[T] = m

    extension [T <: Struct: Sized](p: JfmaPointer[T]) {
      def →[U >: T <: Struct]: Pointer.StructPointerSelectable[U, p.type] = Pointer.StructPointerSelectable(Pointer.unsafe(Address.Zero))
    }

    given [T]: Sized[JfmaPointer[T]] = Sized.apply(ValueLayout.ADDRESS.byteSize())

    given [T]: Ser[JfmaPointer[T]] with {
      type In[Mem] = JfmaPointer[T]
      def write[Mem: MemorySegment](mem: Mem)(p: Address, v: JfmaPointer[T]): Unit = mem.setLong(p, v.address())
    }

    given [T]: Des[JfmaPointer[T]] with {
      type Out[Mem] = JfmaPointer[T]
      def read[Mem: MemorySegment](mem: Mem)(p: Address): JfmaPointer[T] =
        java.lang.foreign.MemorySegment.ofAddress(mem.getLong(p)).reinterpret(Long.MaxValue)
    }
  }
}
