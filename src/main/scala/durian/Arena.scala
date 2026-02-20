package durian

import java.lang.foreign as jfm
import durian.Pointer.PointerSelectable

import Arena.*

class Arena(arena: jfm.Arena) {
  
  /** Allocates an `S` and return a pointer to it.*/
  def allocStruct[S <: Struct: Sized]: AllocatedStruct[S] = {
    val segment = arena.allocate(Sized.of[S].size)
    AllocatedStruct(JfmMemorySegment(segment))
  }
}

object Arena {
  case class AllocatedStruct[S <: Struct](segment: JfmMemorySegment) {
    val allocator = Allocator.NoOp(segment)
    val value: Pointer[S, AllocatedStructAllocator[S, this.type]] = Pointer(Address.Zero)
  }
  given Conversion[AllocatedStruct[?], jfm.MemorySegment] = _.segment.memorySegment

  opaque type AllocatedStructAllocator[S <: Struct, AS <: AllocatedStruct[S]] <: Allocator = Allocator
  object AllocatedStructAllocator {
    given [S <: Struct, AS <: AllocatedStruct[S]](using v: ValueOf[AS]): AllocatedStructAllocator[S, AS] = v.value.allocator
  }
}
