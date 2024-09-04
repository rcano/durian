package durian

import scala.language.experimental.modularity

trait Allocator {
  val memorySegment: MemorySegment
  
  /** Request `size` bytes to be reserved.
    * @return The address for the allocated bytes within the memorySegment
    */
  def alloc(size: Long): Address

  /** Allocate as many bytes as required by the Sized `S`.*/
  def alloc[S: Sized as s]: Address = alloc(s.size)

  /** Allocates an `S` and return a pointer to it.*/
  def allocStruct[S <: Struct: Sized]: Pointer[S, this.type] = Pointer(alloc(summon[Sized[S]].size))
}

object Allocator {
  class Bump(val memorySegment: MemorySegment) extends Allocator {
    private var lastOffset: Address = Address.Zero
    override def alloc(size: Long): Address = {
      val res = lastOffset
      lastOffset += size
      res
    }
  }
}