==========
The Logist
==========
    log parser and error extractor for Granite & CQ (and other sling apps) error.log file


Output: the logist will generate an output file for each log level is encounters. Special care is taken for the error stack traces
which are merged together to provide a count of how many times an error has been encountered.

How to run
----------

From the project's root
    $ mvn exec:java -Dexec.mainClass="com.pfalabs.logist.TheLogist" -Dexec.args="--path src/test/resources/test-error.log --config src/test/resources/test-logist.conf"

From the distribution

    $ java -jar thelogist-*.jar --path src/test/resources/test-error.log --config src/test/resources/test-logist.conf

    --path        path to the error log file (required)
    --config      path to the 'logist.conf' file (required for defining ignores)

Ignores
-------
 Ignores can be defined via the config file 'logist.conf' which then needs to be made available on the classpath

```bash
logist.ignore {
    info {                                    # log level
      OsgiInstallerImpl = ["*"]               # thread name = ignores are verified as 'contains' clauses, '*' means ignore all
      "*" = ["test.test.test.test"]           # matches any thread name, ignore '*' means ignore all log entries
    }
}
```

Examples
--------

Given the following input log using the previously defined ignores:

```bash
01.01.2015 09:00:00.000 *INFO* [OsgiInstallerImpl] org.apache.sling.audit.osgi.installer Installed configuration
01.01.2015 09:00:00.000 *INFO* [TestThread] org.apache.sling.audit.osgi.installer Installed configuration
01.01.2015 09:00:00.000 *ERROR* [pool-6-thread-4] org.apache.sling.commons.scheduler.impl.QuartzScheduler Exception
java.lang.NullPointerException: null
    at org.apache.sling.discovery.impl.common.heartbeat.HeartbeatHandler.issueClusterLocalHeartbeat(HeartbeatHandler.java:295)
01.01.2015 09:00:00.000 *ERROR* [pool-6-thread-4] org.apache.sling.commons.scheduler.impl.QuartzScheduler Exception
java.lang.NullPointerException: null
    at org.apache.sling.discovery.impl.common.heartbeat.HeartbeatHandler.issueClusterLocalHeartbeat(HeartbeatHandler.java:295)
01.01.2015 09:00:00.000 *WARN* [MapEntries Update] org.apache.jackrabbit.oak.plugins.index.property.strategy.ContentMirrorStoreStrategy Traversed 132000 nodes using index sling:vanityPath with filter Filter(query=SELECT sling:vanityPath, sling:redirect, sling:redirectStatus FROM sling:VanityPath WHERE sling:vanityPath IS NOT NULL ORDER BY sling:vanityOrder DESC, path=*, property=[sling:vanityPath=])
```

the output will look like the following:

log-info.txt
```bash
01.01.2015,09:00:00.000,info,TestThread,"org.apache.sling.audit.osgi.installer Installed configuration"
```

log-warn.txt
```bash
01.01.2015,09:00:00.000,warn,MapEntries Update,"org.apache.jackrabbit.oak.plugins.index.property.strategy.ContentMirrorStoreStrategy Traversed 132000 nodes using index sling:vanityPath with filter Filter(query=SELECT sling:vanityPath, sling:redirect, sling:redirectStatus FROM sling:VanityPath WHERE sling:vanityPath IS NOT NULL ORDER BY sling:vanityOrder DESC, path=*, property=[sling:vanityPath=])"
```

log-merge-error.txt
```bash
[2] java.lang.NullPointerException: null
    at org.apache.sling.discovery.impl.common.heartbeat.HeartbeatHandler.issueClusterLocalHeartbeat(HeartbeatHandler.java:295)
```
