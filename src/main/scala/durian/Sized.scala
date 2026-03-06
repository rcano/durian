package durian

import scala.compiletime.summonAll
import scala.deriving.Mirror

trait Sized[T] {
  def size: Long
}

object Sized {
  def apply[T](s: Long): Sized[T] = new Sized[T] { def size = s }
  given Sized[Byte] = apply(1)
  given Sized[Int] = apply(4)
  given Sized[Long] = apply(8)
  given Sized[Float] = apply(4)
  given Sized[Double] = apply(8)
  given Sized[Address] = sys.props("os.arch") match {
    case "amd64" | "aarch64" => apply(8)
    case _  => apply(4)
  }
  given [S <: Struct]: Sized[NestedPointer[S]] = Sized.of[Address].asInstanceOf

  trait Max[T] extends Sized[T]
  def max[T](s: Long) = new Max[T] { def size = s }
  trait Sum[T] extends Sized[T]
  def sum[T](s: Long) = new Sum[T] { def size = s }

  inline given [Tup <: Tuple]: Max[Tup] = {
    val sizes = compiletime.summonAll[Tuple.Map[Tup, Sized]]
    val maxSize = sizes.toList.foldLeft(0L)((acc, s) => math.max(acc, s.asInstanceOf[Sized[?]].size))
    max[Tup](maxSize)
  }

  inline given [Tup <: Tuple]: Sum[Tup] = {
    val sizes = compiletime.summonAll[Tuple.Map[Tup, Sized]]
    val totalSize = sizes.toList.foldLeft(0L)((acc, s) => acc + s.asInstanceOf[Sized[?]].size)
    sum[Tup](totalSize)
  }

  transparent inline def of[T](using s: Sized[T]): s.type = s
}
