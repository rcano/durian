package durian

import durian.jfma.JfmMemorySegment

trait PlatformSpecificMemorySegments {
  given MemorySegment[java.lang.foreign.MemorySegment] = JfmMemorySegment
}
