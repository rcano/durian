package durian

import language.experimental.erasedDefinitions

import scala.compiletime.ops.int.*
import scala.compiletime.ops.string.{Length, Substring}

private[durian] object Util {
  type IndexOf[V, Tup <: Tuple] <: Int = Tup match {
    case head *: tail => head match {
      case V => 0
      case _ => S[IndexOf[V, tail]]
    }
  }

  type StringDropRight[S <: String, V <: Int] = Substring[S, 0, Length[S] - V]

  class TypeCapture[T]() extends compiletime.Erased { 
    type Out = T
  }
  object TypeCapture {
    inline given [T]: TypeCapture[T] = new TypeCapture[T]()
    def apply[T]: TypeCapture[T] = new TypeCapture[T]()
  }
}
