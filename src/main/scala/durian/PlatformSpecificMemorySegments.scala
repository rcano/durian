package durian

trait PlatformSpecificMemorySegments {
  given MemorySegment[java.lang.foreign.MemorySegment] = JfmMemorySegment
}
