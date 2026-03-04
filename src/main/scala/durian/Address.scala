package durian

opaque type Address = Long
object Address {
  val Zero: Address = 0
  def unsafe(addr: Long): Address = addr

  extension (a: Address) {
    def value: Long = a
    def +(b: Address): Address = a + b
  }
}
