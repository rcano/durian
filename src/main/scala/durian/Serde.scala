package durian

import language.experimental.modularity
import scala.annotation.implicitNotFound

@implicitNotFound("Cannot serialize/deserialize type ${T}")
trait Serde[T] {
  final type Out = T
  def read(ctx: MemorySegment)(p: Address): T
  def write(ctx: MemorySegment)(p: Address, v: T): Unit
}
object Serde {
  given Serde[Byte] with {
    def read(ctx: MemorySegment)(p: Address): Byte = ctx.getByte(p)
    def write(ctx: MemorySegment)(p: Address, v: Byte): Unit = ctx.setByte(p, v)
  }
  given Serde[Int] with {
    def read(ctx: MemorySegment)(p: Address): Int = ctx.getInt(p)
    def write(ctx: MemorySegment)(p: Address, v: Int): Unit = ctx.setInt(p, v)
  }
  given Serde[Long] with {
    def read(ctx: MemorySegment)(p: Address): Long = ctx.getLong(p)
    def write(ctx: MemorySegment)(p: Address, v: Long): Unit = ctx.setLong(p, v)
  }
  given Serde[Float] with {
    def read(ctx: MemorySegment)(p: Address): Float = ctx.getFloat(p)
    def write(ctx: MemorySegment)(p: Address, v: Float): Unit =
      ctx.setFloat(p, v)
  }
  given Serde[Double] with {
    def read(ctx: MemorySegment)(p: Address): Double = ctx.getDouble(p)
    def write(ctx: MemorySegment)(p: Address, v: Double): Unit =
      ctx.setDouble(p, v)
  }

  given [S <: Struct]: Serde[Inlined[S]] = StructSerde.asInstanceOf

  private object StructSerde extends Serde[Inlined[Struct]] {
    def read(ctx: MemorySegment)(p: Address): Inlined[Struct] = p.asInstanceOf[Inlined[Struct]]
    def write(ctx: MemorySegment)(p: Address, v: Inlined[Struct]): Unit =
      throw new IllegalStateException("Cannot write an entire struct, instead you first dereference the field and write to its fields")
  }
}
