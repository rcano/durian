package durian

opaque type Address = Long
object Address {
  def unsafe(l: Long): Address = l
  extension (a: Address) {
    def value: Long = a
    def +(offset: Long | Address): Address = a + offset
  }
  val Zero: Address = 0
}

