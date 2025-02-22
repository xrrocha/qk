package qk

object Utils:
  def normalizeSpace(s: String) = s.trim().split("\\s+").mkString(" ")
end Utils
