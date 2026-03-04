package durian

import language.experimental.modularity
import scala.deriving.Mirror

trait StructLayout[S <: Struct] extends Sized[S] {

  /** Address offset from the starting position of the requested field */
  def offset(fieldIdx: Int): Address
}

trait CompactLayout[S <: Struct] extends StructLayout[S]
object CompactLayout {
  case class LayoutInfo[S <: Struct](size: Long, offsets: Array[Long]) extends CompactLayout[S] {
    def offset(fieldIdx: Int): Address = Address.unsafe(offsets(fieldIdx))
  }
  inline def derived[S <: Struct: Mirror.ProductOf as m]: CompactLayout[S] = {
    val sizes = compiletime.summonAll[Tuple.Map[m.MirroredElemTypes, Sized]].toList
    val offsets = sizes.scanLeft(0L)((acc, f) => acc + f.asInstanceOf[Sized[?]].size)
    LayoutInfo[S](offsets.last, offsets.toArray.init)
  }
}
