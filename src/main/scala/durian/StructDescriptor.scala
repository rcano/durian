package durian

import language.experimental.modularity
import language.experimental.erasedDefinitions

trait StructDescriptor {
  type S <: Struct
  type Layout <: StructLayout[S]
  type Mirror <: Struct.StructOf[S]
  lazy val mirror: Mirror
  given layout: Layout = compiletime.deferred
  given givenMirror: Mirror = mirror
  // given Sized[Self] = compiletime.deferred
  // def offset(field: String): Long

  // type FieldIndex[S <: String & Singleton] = Util.IndexOf[S, mirror.MirroredElemLabels]

  // type FieldType[Idx <: Int] = Tuple.Head[Tuple.Drop[mirror.MirroredElemTypes, Idx]]
  // type FieldTypeByName[S <: String & Singleton] = FieldType[FieldIndex[S]]

}

trait StructDescriptor2[S <: Struct, Layout <: StructLayout[S], Mirror <: Struct.StructOf[S]](struct: Util.TypeCapture[S])(using tracked val mirror: Mirror, tracked val layout: Layout)