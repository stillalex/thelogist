package com.pfalabs.logist

import java.io.{ FileOutputStream, PrintWriter }
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success, Try }
import com.pfalabs.logist.ErrLine.keyAsTrace
import com.typesafe.config.{ Config, ConfigFactory }
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigUtil

object TheLogist extends Ignores with CLIRunner {

  val DMYPattern = """(\d\d).(\d\d).(\d\d\d\d) """.r

  def parse(f: String, name: Option[String] = None, cfg: Config = ConfigFactory.empty) = {
    println("TheLogist")
    println("  config " + printConfigs(cfg))
    println("  parsing log " + f)
    if (name.isDefined) {
      println("  name " + name.get)
    }
    extract(f, name, cfg);
  }

  private def extract(f: String, name: Option[String], cfg: Config = ConfigFactory.empty) = {
    val start = System.currentTimeMillis()
    // execution context
    import system.dispatcher

    implicit val system = ActorSystem("TheLogist")
    implicit val matr = ActorMaterializer()

    val logFile = io.Source.fromFile(f, "utf-8")

    val fullLog = Source(() ⇒ logFile.getLines())

      // group by possible error stack traces
      .splitWhen(l ⇒ DMYPattern.findFirstIn(l).isDefined)
      // (string line) => LogLine
      .map(l ⇒ {
        val buffer = l
          .runFold(List[String]())((li, s) ⇒ {
            val ts = s.trim()
            if (!ts.isEmpty()) {
              ts :: li
            } else {
              li
            }
          })

        // TODO find a non-blocking way
        Await.result(buffer, Duration.Inf)
          .reverse match {
            case x :: y ⇒ LogLine(x, y)
            case _      ⇒ NoneLine
          }
      })

      // filter out ignores
      .filter(l ⇒ !ignore(l, cfg))

      // LogLine => (level, LogLine)
      .groupBy {
        case ErrLine(i, l) ⇒ "error"
        case _             ⇒ "regular"
      }

    val res = fullLog
      .runForeach {
        case ("regular", lineFlow) ⇒ lineFlow
          .groupBy {
            case RegLine(t) ⇒ getGroupByKey(t.level, cfg)
            case _          ⇒ "unknown"
          }
          .runForeach {
            case (level, lineFlow) ⇒
              val output = new PrintWriter(new FileOutputStream("log-" + level + asFileNameToken(name) + ".txt"), true)
              lineFlow
                .runForeach(line ⇒ output.println(line))
                .onComplete(_ ⇒ Try(output.close()))
          }
        case ("error", lineFlow) ⇒
          val output = new PrintWriter(new FileOutputStream("log-merge-error" + asFileNameToken(name) + ".txt"), true)
          lineFlow
            .map(l ⇒ l.asInstanceOf[ErrLine])
            .groupBy(l ⇒ l.traceAsKey())
            .runForeach {
              case (errAsString, lineFlow) ⇒
                val erragg = lineFlow
                  .runFold((errAsString, 0)) {
                    case ((w, count), _) ⇒ (w, count + 1)
                  }
                erragg.onComplete {
                  case Success(result) ⇒
                    output.println("[" + result._2 + "] " + keyAsTrace(result._1))

                  case Failure(e) ⇒
                    println("Failure: " + e.getMessage)
                }

            }.onComplete { _ ⇒
              // TODO close errors stream
              // Try(output.close())
            }
      }

    res.onComplete { _ ⇒
      Try(logFile.close())
      system.shutdown()
      val dur = System.currentTimeMillis() - start
      println(s"finished in $dur ms.")
    }
  }

  /**
   * Given a level, provides the final group by key for a specific log line.
   * Usually a log level would end up in its own file name, but we can now merge 2 or more logs into the same config file based on configs
   */
  def getGroupByKey(level: String, cfg: Config): String = {
    val o = ConfigUtil.joinPath("logist", "output", level.toLowerCase())
    if (cfg.hasPath(o)) {
      cfg.getString(o)
    } else {
      // no config defined, goes into the same log file
      level
    }
  }

  private def asFileNameToken(name: Option[String]): String = if (name.isDefined) {
    "-" + name.get
  } else {
    ""
  }

  def main(args: Array[String]): Unit = run(args)
}
