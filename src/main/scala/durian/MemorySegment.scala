package durian

trait MemorySegment {
  def getByte(addr: Address): Byte
  def getShort(addr: Address): Short
  def getInt(addr: Address): Int
  def getLong(addr: Address): Long
  def getChar(addr: Address): Char
  def getFloat(addr: Address): Float
  def getDouble(addr: Address): Double
  def getBytes(addr: Address, len: Int): Array[Byte]
  def getUtf8String(addr: Address): String
  def copyBytesInto(addr: Address, len: Int, arr: Array[Byte]): Unit

  def setByte(addr: Address, v: Byte): Unit
  def setShort(addr: Address, v: Short): Unit
  def setInt(addr: Address, v: Int): Unit
  def setLong(addr: Address, v: Long): Unit
  def setChar(addr: Address, v: Char): Unit
  def setFloat(addr: Address, v: Float): Unit
  def setDouble(addr: Address, v: Double): Unit
  def setBytes(addr: Address, bytes: Array[Byte]): Unit
  def setUtf8String(addr: Address, v: String): Unit
}
