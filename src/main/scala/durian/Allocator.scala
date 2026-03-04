package durian

import scala.language.experimental.modularity
import java.lang.foreign as jfm

trait Allocator {
  type Mem
  val memorySegment: Mem
  given MemorySegment[Mem] = scala.compiletime.deferred
  
  /** Request `size` bytes to be reserved.
    * @return The address for the allocated bytes within the memorySegment
    */
  def alloc(size: Long): Address

  /** Allocate as many bytes as required by the Sized `S`.*/
  def alloc[S: Sized as s]: Address = alloc(s.size)

  /** Allocates an `S` and return a pointer to it.*/
  def allocPtr[S: Sized]: Pointer[S, memorySegment.type] = Pointer.unsafe(alloc(summon[Sized[S]].size))
}

object Allocator {
  // given (using alloc: Allocator): MemorySegment[alloc.Mem] = alloc.given_MemorySegment_Mem
  given (using alloc: Allocator): alloc.Mem = alloc.memorySegment
  class Bump[M: MemorySegment](val memorySegment: M) extends Allocator {
    type Mem = M
    private var lastOffset: Address = Address.Zero
    override def alloc(size: Long): Address = {
      val res = lastOffset
      lastOffset += Address.unsafe(size)
      res
    }
  }

  class NoOp[M: MemorySegment](val memorySegment: M) extends Allocator {
    type Mem = M
    def alloc(size: Long): Address = throw new UnsupportedOperationException("No-op allocator can't allocate")
  }
}

case class AllocatedStruct[S <: Struct, M: MemorySegment](segment: M) {
  val value: Pointer[S, segment.type] = Pointer.unsafe(Address.Zero)
}