package durian.jfma

import java.lang.invoke.MethodHandles
import java.lang.foreign.Linker
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.SymbolLookup

object CFuncDsl {
  trait CFunctionBinding[Args <: NamedTuple.AnyNamedTuple, Ret](val symbolName: String, lookup: SymbolLookup) {
    val functionAddress = lookup.findOrThrow(symbolName)
    given descr: CFunctionDescr[this.type] = compiletime.deferred
    lazy val mh = Linker.nativeLinker().downcallHandle(functionAddress, descr.functionDescriptor)
    def apply(params: Args): Ret = {
      val paramsArray = params.asInstanceOf[Tuple].toArray
      for (i <- paramsArray.indices) paramsArray(i) match {
        case u: Upcall[?, ?, ?] => paramsArray(i) = u.upcallAddr
        case _ =>
      }
      mh.invokeWithArguments(paramsArray*).asInstanceOf[Ret]
    }
  }

  class CFunctionDescr[F <: CFunctionBinding[?, ?]](
      val functionDescriptor: FunctionDescriptor,
  )
  object CFunctionDescr {
    inline def derived[Args <: NamedTuple.AnyNamedTuple, Ret, F <: CFunctionBinding[Args, Ret]]: CFunctionDescr[F] =
      CFunctionDescr(LayoutUtils.functionDescriptor[NamedTuple.DropNames[Args], Ret])
  }

  class Upcall[F, Args <: Tuple, Ret](f: F, descr: CFunctionDescr[CFunctionBinding[NamedTuple.From[Args], Ret]])(using
      util.TupledFunction[F, Args => Ret]
  ) {
    val applyHandle = MethodHandles.dropReturn(
      MethodHandles.lookup().unreflect(f.getClass().getMethods().find(_.getName() == "apply").get).bindTo(f)
    )
    val invoker = MethodHandles.invoker(descr.functionDescriptor.toMethodType()).bindTo(applyHandle)
    val upcallAddr = Linker.nativeLinker().upcallStub(invoker, descr.functionDescriptor, java.lang.foreign.Arena.global())
  }
  object Upcall {
    inline def apply[F, Args <: Tuple, Ret](f: F)(using util.TupledFunction[F, Args => Ret]): Upcall[F, Args, Ret] = {
      val descr = CFunctionDescr.derived[NamedTuple.From[Args], Ret, CFunctionBinding[NamedTuple.From[Args], Ret]]
      new Upcall(f, descr)
    }
  }
}
