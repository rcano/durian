package durian

import java.lang.foreign as jfm

class Arena(arena: jfm.Arena) {
  
  /** Allocates an `S` and return a pointer to it.*/
  def allocStruct[S <: Struct: Sized]: AllocatedStruct[S, jfm.MemorySegment] = {
    val segment = arena.allocate(Sized.of[S].size)
    AllocatedStruct(segment)
  }
}
