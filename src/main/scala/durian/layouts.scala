package durian

import language.experimental.modularity

trait StructLayout[S <: Struct] extends Sized[S] {

  /** Address offset from the starting position of the requested field */
  def offset(fieldIdx: Int): Address
}

object CompactLayouter {
  case class CompactStructLayout[S <: Struct](size: Long, offsets: Array[Long]) extends StructLayout[S] {
    def offset(fieldIdx: Int): Address = Address.unsafe(offsets(fieldIdx))
  }
  inline def compactLayout[S <: Struct: Struct.StructOf as m]: StructLayout[S] = {
    val sizes = compiletime.summonAll[Tuple.Map[m.MirroredElemTypes, Sized]].toList
    val offsets = sizes.scanLeft(0L)((acc, f) => acc + f.asInstanceOf[Sized[?]].size)
    CompactStructLayout[S](offsets.last, offsets.toArray.init)
  }
}
