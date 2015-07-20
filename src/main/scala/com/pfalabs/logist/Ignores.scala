package com.pfalabs.logist

import scala.collection.JavaConversions.asScalaBuffer

import com.typesafe.config.{ Config, ConfigRenderOptions, ConfigUtil }

trait Ignores {

  def ignore(line: LogLine, cfg: Config): Boolean = line match {
    case RegLine(t)    ⇒ ignore(t, cfg)
    case ErrLine(t, e) ⇒ ignore(t, cfg)
    case _             ⇒ false
  }

  def ignore(t: Tokens, cfg: Config): Boolean = {
    val p1 = ConfigUtil.joinPath("logist", "ignore", t.level.toLowerCase(), t.thread)
    if (cfg.hasPath(p1)) {
      val ign = cfg.getStringList(p1)
      if (ign.contains("*")) {
        return true;
      }
      ign.foreach { i ⇒ if (t.info.contains(i)) { return true } }
    }
    val p2 = ConfigUtil.joinPath("logist", "ignore", t.level.toLowerCase(), "*")
    if (cfg.hasPath(p2)) {
      val ign = cfg.getStringList(p2)
      if (ign.contains("*")) {
        return true;
      }
      ign.foreach { i ⇒ if (t.info.contains(i)) { return true } }
    }

    return false
  }

  def printConfigs(cfg: Config): String = {
    if (cfg.hasPath("logist")) {
      cfg.atPath("logist").root().render(ConfigRenderOptions.concise().setFormatted(true))
    } else {
      "using default configuration"
    }
  }

}