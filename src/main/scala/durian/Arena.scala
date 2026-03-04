package durian

import java.lang.foreign as jfm

class Arena(arena: jfm.Arena) {
  
  /** Allocates an `S` and return a pointer to it.*/
  def allocStruct[S <: Struct: Sized]: jfma.JfmaPointer[S] = {
    val segment = arena.allocate(Sized.of[S].size)
    jfma.JfmaPointer.unsafe(segment)
  }
}
