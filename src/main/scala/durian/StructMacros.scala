package durian

import language.experimental.{modularity}

import scala.deriving.Mirror
import scala.quoted.{Expr, Quotes, Type}
import scala.reflect.TypeTest

private[durian] object StructMacros {
  def structMirror[S <: Struct: Type](using Quotes): Expr[Struct.StructOf[S]] = StructMacros().structMirror[S]
}

private class StructMacros private[StructMacros] (using val quotes: Quotes) {
  import quotes.reflect._

  def structMirror[S <: Struct: Type]: Expr[Struct.StructOf[S]] = {
    val sType = TypeRepr.of[S]
    val (recordTypeRepr, refinements) = findRefinedType(sType, sType)
      .getOrElse(report.errorAndAbort(s"Cannot build struct mirror for non refined type $sType"))

    val recordName = recordTypeRepr.typeSymbol.name
    val fields = refinementsList(refinements).reverse
    val recordNameType = ConstantType(StringConstant(recordName)).asType.asInstanceOf[Type[String]]
    val elemTypes = buildTupleOfTypes(fields.map(_._2)).asInstanceOf[Type[Tuple]]
    val elemNames = buildTupleOfTypes(fields.map(t => ConstantType(StringConstant(t._1)))).asInstanceOf[Type[Tuple]]

    val recordType = recordTypeRepr.asType.asInstanceOf[Type[Struct]]
    '{
      null.asInstanceOf[
        Struct.StructOf[S] {
          type MirroredMonoType = S
          type MirroredType = recordType.Underlying
          type MirroredLabel = recordNameType.Underlying
          type MirroredElemTypes = elemTypes.Underlying
          type MirroredElemLabels = elemNames.Underlying
        }
      ]
    }
  }

  def findRefinedType(t: TypeRepr, typeOwner: TypeRepr): Option[(TypeRepr, Refinement)] = t match {
    case r: Refinement => Some(typeOwner -> r)
    case tr: TypeRef => 
      // println(s"daliased ${tr.dealias}")
      if tr.dealias != tr then findRefinedType(tr.dealias, tr) else None
    case AndType(a, b) => findRefinedType(a, typeOwner) orElse findRefinedType(b, typeOwner)
    case _ => None
  }

  /** find out the "properties" refined. Only those with getters (with optional setter) are valid */
  def refinementsList(r: Refinement): List[(String, TypeRepr)] = {
    val parents = r.parent match
      case p: Refinement => refinementsList(p)
      case _ => Nil

    if (summon[TypeTest[TypeRepr, MethodType]].unapply(r.info).isDefined) parents
    else {
      val fieldTpe = r.info match {
        case ByNameType(tpe) => tpe
        case other => other
      }
      // println(s"Producing ${r.name}: ${fieldTpe.show}")
      (r.name -> fieldTpe) :: parents
    }
  }

  def buildTupleOfTypes(types: Seq[TypeRepr]): Type[? <: Tuple] = {
    if types.isEmpty then Type.of[EmptyTuple]
    else {
      val tail = buildTupleOfTypes(types.tail).asInstanceOf[Type[Tuple]]
      val head = types.head.asType.asInstanceOf[Type[Any]]
      Type.of[head.Underlying *: tail.Underlying]
    }
  }
}
