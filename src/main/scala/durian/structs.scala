package durian

import language.experimental.modularity
import language.experimental.erasedDefinitions
import compiletime.ops.string.Substring

opaque type Pointer[S <: Struct, Allocator <: durian.Allocator] <: S = S
object Pointer {
  def apply[S <: Struct, Allocator <: durian.Allocator](a: Address): Pointer[S, Allocator] = a.asInstanceOf

  extension [S <: Struct, A <: durian.Allocator](p: Pointer[S, A]) def memAddress: Address = p

  implicit class PointerSelectable[S <: Struct, A <: Allocator](private val p: Pointer[S, A]) extends AnyVal with Selectable {
    inline def selectDynamic(using mirror: Struct.StructOf[S], layout: StructLayout[S], allocator: A)(
        f: String & Singleton
    )(using posType: Util.TypeCapture[Util.IndexOf[f.type, mirror.MirroredElemLabels]])(using
        index: ValueOf[posType.Out],
        serde: Serde[Tuple.Head[Tuple.Drop[mirror.MirroredElemTypes, posType.Out]]]
    ): serde.Out = serde.read(allocator.memorySegment)(p.memAddress + layout.offset(index.value))

    inline def applyDynamic(using mirror: Struct.StructOf[S], layout: StructLayout[S], allocator: A)(
        f: String & Singleton
    )(using erased posType: Util.TypeCapture[Util.IndexOf[Util.StringDropRight[f.type, 4], mirror.MirroredElemLabels]])(using
        index: ValueOf[posType.Out],
        serde: Serde[Tuple.Head[Tuple.Drop[mirror.MirroredElemTypes, posType.Out]]]
    )(value: serde.Out): Unit = serde.write(allocator.memorySegment)(p.memAddress + layout.offset(index.value), value)
  }

}

opaque type Inlined[S <: Struct] <: S = S
object Inlined {
  inline given [S <: Struct](using s: Sized[S]): Sized[Inlined[S]] = s.asInstanceOf
  given inlinedAsPointer[S <: Struct](using ctx: Allocator): Conversion[Inlined[S], Pointer.PointerSelectable[S, ctx.type]] =
    Pointer.PointerSelectable(_)

}

/** Struct definition. It cannot be inherited, only refined */
opaque type Struct = Address
object Struct {

  /** Custom mirror type for Structs. Main difference with ProductOf is that MirroredType does not reflect the struct source, because this
    * can be either a `Pointer[?] & Struct` or the actual struct and we need the actual struct
    */
  type StructOf[T] = deriving.Mirror.Product { type MirroredType; type MirroredMonoType = T; type MirroredElemTypes <: Tuple }
  transparent inline def structMirror[S <: Struct]: StructOf[S] = ${ StructMacros.structMirror[S] }

  // inline def derivedSized[S <: Struct](using m: StructOf[S]): Sized[S] = {
  //   val res = compiletime.summonAll[Tuple.Map[m.MirroredElemTypes, Sized]].toList.foldLeft(0)((acc, f) => acc + f.asInstanceOf[Sized[?]].size)
  //   Sized[S](res)
  // }

}
