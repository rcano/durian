package durian

import java.lang.foreign.ValueLayout

class JfmMemorySegment(val memorySegment: java.lang.foreign.MemorySegment) extends MemorySegment {
  override def getBytes(addr: Address, len: Int): Array[Byte] = memorySegment.asSlice(addr.value, len).toArray(ValueLayout.JAVA_BYTE)
  override def getByte(addr: Address): Byte = memorySegment.get(ValueLayout.JAVA_BYTE, addr.value)
  override def getShort(addr: Address): Short = memorySegment.get(ValueLayout.JAVA_SHORT_UNALIGNED, addr.value)
  override def getInt(addr: Address): Int = memorySegment.get(ValueLayout.JAVA_INT_UNALIGNED, addr.value)
  override def getChar(addr: Address): Char = memorySegment.get(ValueLayout.JAVA_CHAR_UNALIGNED, addr.value)
  override def getLong(addr: Address): Long = memorySegment.get(ValueLayout.JAVA_LONG_UNALIGNED, addr.value)
  override def getFloat(addr: Address): Float = memorySegment.get(ValueLayout.JAVA_FLOAT_UNALIGNED, addr.value)
  override def getDouble(addr: Address): Double = memorySegment.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, addr.value)
  override def getUtf8String(addr: Address): String = memorySegment.getUtf8String(addr.value)

  override def setByte(addr: Address, v: Byte): Unit = memorySegment.set(ValueLayout.JAVA_BYTE, addr.value, v)
  override def setShort(addr: Address, v: Short): Unit = memorySegment.set(ValueLayout.JAVA_SHORT_UNALIGNED, addr.value, v)
  override def setInt(addr: Address, v: Int): Unit = memorySegment.set(ValueLayout.JAVA_INT_UNALIGNED, addr.value, v)
  override def setChar(addr: Address, v: Char): Unit = memorySegment.set(ValueLayout.JAVA_CHAR_UNALIGNED, addr.value, v)
  override def setLong(addr: Address, v: Long): Unit = memorySegment.set(ValueLayout.JAVA_LONG_UNALIGNED, addr.value, v)
  override def setFloat(addr: Address, v: Float): Unit = memorySegment.set(ValueLayout.JAVA_FLOAT_UNALIGNED, addr.value, v)
  override def setDouble(addr: Address, v: Double): Unit = memorySegment.set(ValueLayout.JAVA_DOUBLE_UNALIGNED, addr.value, v)
  override def setBytes(addr: Address, bytes: Array[Byte]): Unit =
    memorySegment.asSlice(addr.value, bytes.length).copyFrom(java.lang.foreign.MemorySegment.ofArray(bytes))
  override def setUtf8String(addr: Address, v: String): Unit = memorySegment.setUtf8String(addr.value, v)
  override def copyBytesInto(addr: Address, len: Int, arr: Array[Byte]): Unit =
    java.lang.foreign.MemorySegment.copy(memorySegment, ValueLayout.JAVA_BYTE, addr.value, arr, 0, arr.length)
}
