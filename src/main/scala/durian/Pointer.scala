package durian

import scala.language.implicitConversions
import scala.language.experimental.modularity
import java.lang.{foreign => jfm}

/** Top level pointer of type T within some memory bank Mem */
opaque type Pointer[-T, +Mem] = Address
object Pointer {

  def unsafe[T, Mem](a: Address): Pointer[T, Mem] = a

  extension [T, Mem: Precise](p: Pointer[T, Mem]) {
    def pointerAddress: Address = p

    inline def apply()(using s: Des[T], memSeg: MemorySegment[Mem]): s.Out[Mem] = {
      val mem = implicitMem[Mem]
      s.read(mem)(p)
    }

    inline def :=(using s: Ser[T], memSeg: MemorySegment[Mem])(v: s.In[Mem]): Unit = {
      val mem = implicitMem[Mem]
      s.write(mem)(p, v)
    }

    def →[U >: T <: Struct]: StructPointerSelectable[U, Mem] = StructPointerSelectable(p)
  }

  private inline def implicitMem[Mem] = scala.compiletime.summonFrom {
    case memorySegment: Mem => memorySegment
    case memorySegment: ValueOf[Mem] => memorySegment.value
    case _ => compiletime.error("No instance for the memory segment " + Util.typeDescr[Mem] + " was found")
  }

  implicit def toSelectable[S <: Struct, Mem](p: Pointer[S, Mem]): StructPointerSelectable[S, Mem] = StructPointerSelectable(p)

  implicit class StructPointerSelectable[S <: Struct, Mem](private val p: Pointer[S, Mem]) extends AnyVal, Selectable {
    type Fields = NamedTuple.Map[NamedTuple.From[S], [v] =>> Pointer[v, Mem]]

    inline def selectDynamic(field: String & Singleton)(using
        indexTpe: Util.TypeCapture[Util.IndexOf[field.type, NamedTuple.Names[Fields]]],
        layout: StructLayout[S]
    ): NamedTuple.Elem[Fields, indexTpe.Out] = {
      val index = compiletime.constValue[indexTpe.Out]
      Pointer.unsafe(p + layout.offset(index)).asInstanceOf[NamedTuple.Elem[Fields, indexTpe.Out]]
    }
  }
}
