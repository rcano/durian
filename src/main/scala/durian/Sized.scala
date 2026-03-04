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

  transparent inline def of[T](using s: Sized[T]): s.type = s
}
