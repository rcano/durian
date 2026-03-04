package durian

/** Special compatibility types for working with java foreign memory and access layer
  * 
  */
package object jfma {
  
  opaque type JfmaPointer[T] <: java.lang.foreign.MemorySegment = java.lang.foreign.MemorySegment

  object JfmaPointer {

    def unsafe[T](m: java.lang.foreign.MemorySegment): JfmaPointer[T] = m

    extension [T <: Struct: Sized](p: JfmaPointer[T]) {
      def →[U >: T <: Struct]: Pointer.StructPointerSelectable[U, p.type] = Pointer.StructPointerSelectable(Pointer.unsafe(Address.Zero))
    }
  }
}
