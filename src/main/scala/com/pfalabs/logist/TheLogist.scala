package com.pfalabs.logist

import java.io.{ FileOutputStream, PrintWriter }

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success, Try }

import com.pfalabs.logist.ErrLine.keyAsTrace
import com.typesafe.config.{ Config, ConfigFactory }

import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Source

object TheLogist extends Ignores with CLIRunner {

  val DMYPattern = """(\d\d).(\d\d).(\d\d\d\d) """.r

  def parse(f: String, cfg: Config = ConfigFactory.empty) = {
    val t = System.currentTimeMillis()
    println("TheLogist")
    println("  config " + printIgnores(cfg))
    println("  log " + f)

    extract(f, cfg);

    val dur = System.currentTimeMillis() - t
    println(s"finished in $dur ms.")
  }

  private def extract(f: String, cfg: Config = ConfigFactory.empty) = {
    // execution context
    import system.dispatcher

    implicit val system = ActorSystem("TheLogist")
    implicit val matr = ActorFlowMaterializer()

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
            case RegLine(t) ⇒ t.level
            case _          ⇒ "unknown"
          }
          .runForeach {
            case (level, lineFlow) ⇒
              val output = new PrintWriter(new FileOutputStream(s"log-$level.txt"), true)
              lineFlow
                .runForeach(line ⇒ output.println(line))
                .onComplete(_ ⇒ Try(output.close()))
          }
        case ("error", lineFlow) ⇒
          val output = new PrintWriter(new FileOutputStream("log-errors.txt"), true)
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
    }
  }

  def main(args: Array[String]): Unit = run(args)
}
