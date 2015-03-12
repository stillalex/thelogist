package com.pfalabs.logist

import com.typesafe.config.ConfigFactory
import scala.annotation.tailrec
import com.typesafe.config.Config
import java.io.File

trait CLIRunner {

  val usage = """
    Usage: java -jar thelogist-*.jar --path error.log [--config thelogist.conf --name test]
  """

  def runExample(args: Array[String]) {
    val cfg: Config = ConfigFactory.load("test-logist")
    val path = "src/test/resources/test-error.log"
    TheLogist.parse(path, None, cfg)
  }

  def run(args: Array[String]) {
    if (args.length == 0) {
      println(usage)
      return
    }
    val arglist = extractParams(args.grouped(2), Map())
    if (arglist.get("path").isEmpty) {
      println(usage)
      return
    }
    val path = arglist.get("path").get

    val cfg: Config = arglist.get("config") match {
      case Some(c) ⇒ ConfigFactory.parseFile(new File(c))
      case _       ⇒ ConfigFactory.load("thelogist")
    }

    TheLogist.parse(path, arglist.get("name"), cfg)

  }

  @tailrec
  private def extractParams(args: Iterator[Array[String]], map: Map[String, String]): Map[String, String] = {
    if (!args.hasNext) {
      return map
    }
    val opt = args.next()
    extractParams(args, map ++ Map(opt(0).replaceFirst("--", "") -> opt(1)))
  }

}