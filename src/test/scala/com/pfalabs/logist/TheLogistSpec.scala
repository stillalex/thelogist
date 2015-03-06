package com.pfalabs.logist

import org.scalatest.{ FlatSpec, Matchers }

import com.typesafe.config.ConfigFactory

class TheLogistSpec extends FlatSpec with Matchers {

  "Config" should "support ignores" in {

    val cfg = ConfigFactory.load("test-logist")
    TheLogist.ignore(LogLine("26.02.2015 10:16:44.891 *INFO* [OsgiInstallerImpl] org.apache.sling.audit.osgi.installer Installed configuration"), cfg) should be(true)
    TheLogist.ignore(LogLine("26.02.2015 10:16:44.891 *INFO* [TestThread] org.apache.sling.audit.osgi.installer Installed configuration"), cfg) should be(true)

    val emt = ConfigFactory.load("doesn-t-exist")
    TheLogist.ignore(LogLine("26.02.2015 10:16:44.891 *INFO* [OsgiInstallerImpl] org.apache.sling.audit.osgi.installer Installed configuration"), emt) should be(false)

  }

}