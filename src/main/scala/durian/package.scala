package object durian {

  inline def using[T, R](t: T)(f: T ?=> R): R = f(using t)
}

