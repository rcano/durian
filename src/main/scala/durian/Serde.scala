package durian

import language.experimental.modularity
import scala.annotation.implicitNotFound

@implicitNotFound("Cannot serialize type ${T}")
trait Ser[T] {
  type In[Mem]
  def write[Mem: MemorySegment](mem: Mem)(p: Address, v: In[Mem]): Unit
}
object Ser {
  given Ser[Byte] with {
    type In[Mem] = Byte
    def write[Mem: MemorySegment](mem: Mem)(p: Address, v: Byte): Unit = mem.setByte(p, v)
  }
  given Ser[Int] with {
    type In[Mem] = Int
    def write[Mem: MemorySegment](mem: Mem)(p: Address, v: Int): Unit = mem.setInt(p, v)
  }
  given Ser[Long] with {
    type In[Mem] = Long
    def write[Mem: MemorySegment](mem: Mem)(p: Address, v: Long): Unit = mem.setLong(p, v)
  }
  given Ser[Float] with {
    type In[Mem] = Float
    def write[Mem: MemorySegment](mem: Mem)(p: Address, v: Float): Unit = mem.setFloat(p, v)
  }
  given Ser[Double] with {
    type In[Mem] = Double
    def write[Mem: MemorySegment](mem: Mem)(p: Address, v: Double): Unit = mem.setDouble(p, v)
  }

  given [U](using s: Sized[NestedPointer[U]]): Ser[NestedPointer[U]] with {
    type In[Mem] = Pointer[U, Mem]
    def write[Mem: MemorySegment](mem: Mem)(p: Address, v: Pointer[U, Mem]) = s.size match {
      case 8 => mem.getLong(p)
      case 4 => mem.getInt(p)
      case other => throw new IllegalStateException(s"Pointer of size $other is not supported")
    }
  }
}

@implicitNotFound("Cannot deserialize type ${T}")
trait Des[T] {
  type Out[Mem]
  def read[Mem: MemorySegment](mem: Mem)(p: Address): Out[Mem]
}

object Des {
  given Des[Byte] with {
    type Out[Mem] = Byte
    def read[Mem: MemorySegment](mem: Mem)(p: Address): Byte = mem.getByte(p)
  }
  given Des[Int] with {
    type Out[Mem] = Int
    def read[Mem: MemorySegment](mem: Mem)(p: Address): Int = mem.getInt(p)
  }
  given Des[Long] with {
    type Out[Mem] = Long
    def read[Mem: MemorySegment](mem: Mem)(p: Address): Long = mem.getLong(p)
  }
  given Des[Float] with {
    type Out[Mem] = Float
    def read[Mem: MemorySegment](mem: Mem)(p: Address): Float = mem.getFloat(p)
  }
  given Des[Double] with {
    type Out[Mem] = Double
    def read[Mem: MemorySegment](mem: Mem)(p: Address): Double = mem.getDouble(p)
  }

  given [U](using s: Sized[NestedPointer[U]]): Des[NestedPointer[U]] with {
    type Out[Mem] = Pointer[U, Mem]
    def read[Mem: MemorySegment](mem: Mem)(p: Address) = s.size match {
      case 8 => mem.getLong(p).asInstanceOf[Out[Mem]]
      case 4 => mem.getInt(p).asInstanceOf[Out[Mem]]
      case other => throw new IllegalStateException(s"Pointer of size $other is not supported")
    }
  }
}
