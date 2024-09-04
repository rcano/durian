package durian

trait Sized[T] {
  def size: Long
}

object Sized {
  def apply[T](s: Int): Sized[T] = new Sized[T] { def size = s }
  given Sized[Byte] = apply(1)
  given Sized[Int] = apply(4)
  given Sized[Long] = apply(8)
  given Sized[Float] = apply(4)
  given Sized[Double] = apply(8)

  transparent inline def of[T](using s: Sized[T]): s.type = s
}
