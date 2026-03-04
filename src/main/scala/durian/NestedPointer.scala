package durian

/** A NestedPointer is less generic than a Pointer, and it is used to mark a field inside a Struct as a pointer.
  *
  * The difference in the type symbolizes that it cannot point to a different memory bank.
  */
opaque type NestedPointer[-T] = Address
