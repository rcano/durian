package durian
package jfma

import java.lang.foreign.{
  FunctionDescriptor,
  MemoryLayout,
  ValueLayout
}
import scala.compiletime.*
import scala.deriving.Mirror
import scala.quoted.*

object LayoutUtils {
  val C_POINTER = ValueLayout.ADDRESS
    .withTargetLayout(MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, ValueLayout.JAVA_BYTE))

  type ToNativeAdapter[F] = F match {
    case JfmaPointer[s] => s
    case _ => F
  }

  inline def argsToLayouts[Args <: Tuple]: List[MemoryLayout] = {
    inline erasedValue[Args] match {
      case _: EmptyTuple => Nil
      case _: (head *: t) => memoryLayoutFor[head] :: argsToLayouts[t]
    }
  }

  inline def memoryLayoutFor[T]: MemoryLayout = inline erasedValue[ToNativeAdapter[T]] match {
    case _: Struct => // we have to specifically separate the struct from the rest, because otherwise for whatever reason the compiler fails to pat-mat on Upcall
      summonFrom { case prodMirror: Mirror.ProductOf[ToNativeAdapter[T]] =>
        val fields = argsToLayouts[prodMirror.MirroredElemTypes]
        MemoryLayout.structLayout(fields*)
      }
    case _ =>
      inline erasedValue[T] match {
        case _: Byte => ValueLayout.JAVA_BYTE
        case _: Short => ValueLayout.JAVA_SHORT
        case _: Char => ValueLayout.JAVA_CHAR
        case _: Int => ValueLayout.JAVA_INT
        case _: Long => ValueLayout.JAVA_LONG
        case _: Float => ValueLayout.JAVA_FLOAT
        case _: Double => ValueLayout.JAVA_DOUBLE
        case _: Boolean => ValueLayout.JAVA_BOOLEAN
        case _: java.lang.foreign.MemorySegment => C_POINTER
        case _: CFuncDsl.Upcall[?, ?, ?] => C_POINTER
        case _ =>
          error("Don't know what memory layout corresponds to " + typeDescrOf[T])
      }
  }

  private inline def typeDescrOf[T]: String = ${ typeDescrOfMacro[T] }
  private def typeDescrOfMacro[T: Type](using Quotes): Expr[String] = {
    import quotes.reflect.*
    Literal(StringConstant(TypeRepr.of[T].show)).asExprOf[String]
  }

  inline def structDescriptor[P: Mirror.ProductOf as p]: java.lang.foreign.StructLayout = MemoryLayout.structLayout(argsToLayouts[p.MirroredElemTypes]*)

  inline def functionDescriptor[Args <: Tuple, Ret]: FunctionDescriptor = {
    val argsLayouts = argsToLayouts[Args].toArray
    inline erasedValue[Ret] match {
      case _: Unit => FunctionDescriptor.ofVoid(argsLayouts*)
      case _ => FunctionDescriptor.of(memoryLayoutFor[Ret], argsLayouts*)
    }
  }
}
