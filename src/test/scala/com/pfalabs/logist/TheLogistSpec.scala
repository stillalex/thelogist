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

  "TheLogist" should "parse" in {

    val t1 = LogLine("26.02.2015 10:16:44.891 *INFO* [OsgiInstallerImpl] org.apache.sling.audit.osgi.installer Installed configuration").asInstanceOf[RegLine].t
    t1.level should be("info")
    t1.thread should be("OsgiInstallerImpl")
    t1.info should be("org.apache.sling.audit.osgi.installer Installed configuration")

    val t2 = LogLine("22.02.2015 03:03:28.256 *INFO* [FelixStartLevel] org.apache.felix.jaas Registering LoginModule class [org.apache.jackrabbit.oak.security.authentication.token.TokenLoginModule] from Bundle org.apache.jackrabbit.oak-core [85]").asInstanceOf[RegLine].t
    t2.level should be("info")
    t2.thread should be("FelixStartLevel")
    t2.info should be("org.apache.felix.jaas Registering LoginModule class [org.apache.jackrabbit.oak.security.authentication.token.TokenLoginModule] from Bundle org.apache.jackrabbit.oak-core [85]")

    val t3 = LogLine("22.02.2015 10:32:09.828 *WARN* [192.168.0.1 [azerty] GET /bin/custom/hc HTTP/1.1] com.pfalabs.logist.TheLogist Unable to parse logs, path=/bin/custom/hc").asInstanceOf[RegLine].t
    t3.level should be("warn")
    t3.thread should be("192.168.0.1 [azerty] GET /bin/custom/hc HTTP/1.1")
    t3.info should be("com.pfalabs.logist.TheLogist Unable to parse logs, path=/bin/custom/hc")

  }
}