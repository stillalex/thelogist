package com.pfalabs.logist

trait LogLine {}

object LogLine {

  val linePattern = """(\d*.\d*.\d*) (\d*:\d*:\d*.\d*) \*(.*)\* \[(.*)\] (.*)""".r

  def parse(l: String): Option[Tokens] = l match {
    case linePattern(a, b, c, d, e) ⇒ Some(Tokens(a, b, c, d, e))
    case _                          ⇒ None
  }

  def apply(l: String, errs: List[String] = List()): LogLine = parse(l) match {
    case Some(t) if errs.isEmpty ⇒ RegLine(t)
    case Some(t)                 ⇒ ErrLine(t, errs)
    case _                       ⇒ NoneLine
  }
}

case class Tokens(dmy: String, ts: String, level: String, thread: String, what: String) {
  override def toString(): String = s"""$dmy,$ts,$level,thread,"$what""""
}
case class RegLine(t: Tokens) extends LogLine {
  override def toString(): String = t.toString()
}

case class ErrLine(t: Tokens, trace: List[String]) extends LogLine {
  def traceAsKey(): String = trace.mkString("|")

}

object ErrLine {
  def keyAsTrace(key: String): String = key.replace("|", "\n    ")
}

case object NoneLine extends LogLine
