package com.pfalabs.logist

import scala.annotation.tailrec

trait LogLine {}

object LogLine {

  val linePattern = """(\d*.\d*.\d*) (\d*:\d*:\d*.\d*) \*(.*)\* (.*)""".r

  def parse(l: String): Option[Tokens] = l match {
    case linePattern(dmy, ts, level, info) ⇒ {
      val nameInfo = splitNameInfo(info);
      Some(Tokens(dmy, ts, level.toLowerCase(), nameInfo._1, nameInfo._2))
    }
    case _ ⇒ None
  }

  def apply(l: String, errs: List[String] = List()): LogLine = parse(l) match {
    case Some(t) if errs.isEmpty ⇒ RegLine(t)
    case Some(t)                 ⇒ ErrLine(t, errs)
    case _                       ⇒ NoneLine
  }

  private def splitNameInfo(input: String): (String, String) =
    splitNameBySlice(input)

  private def splitNameByChar(input: String): (String, String) =
    if (input.startsWith("[")) {
      // eagerly consume '[' first
      val split = input.tail.foldLeft(("", "", 1))((p, c) ⇒
        c match {
          case '[' if (p._3 > 0) ⇒
            (p._1 + c, p._2, p._3 + 1)
          case ']' if (p._3 > 1) ⇒
            (p._1 + c, p._2, p._3 - 1)
          case ']' if (p._3 == 1) ⇒
            (p._1, p._2, 0)
          case _ if (p._3 > 0) ⇒
            (p._1 + c, p._2, p._3)
          case _ ⇒
            (p._1, p._2 + c, p._3)
        })
      (split._1.trim(), split._2.trim())
    } else {
      ("-", input)
    }

  private def splitNameBySlice(input: String): (String, String) =
    if (input.startsWith("[")) {
      val e = balance(input)
      (input.substring(1, e).trim(), input.substring(e + 1).trim())
    } else {
      ("-", input)
    }

  @tailrec
  private def balance(input: String, opens: Int = 0, closes: Int = 0, delta: Int = 0): Int = {
    val e = input.indexOf(']')
    val i = input.substring(0, e + 1)
    val p = i.foldLeft((0, 0))((p, c) ⇒ if ('[' == c) {
      (p._1 + 1, p._2)
    } else if (']' == c) {
      (p._1, p._2 + 1)
    } else {
      (p._1, p._2)
    })

    if (opens + p._1 == closes + p._2) {
      e + delta
    } else {
      balance(input.substring(e + 1), opens + p._1, closes + p._2, e + delta + 1)
    }
  }

}

case class Tokens(dmy: String, ts: String, level: String, thread: String, info: String) {
  override def toString(): String = s"""$dmy,$ts,$level,$thread,"$info""""
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
