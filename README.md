==========
The Logist
==========

*log parser and error extractor for CQ & Granite (and other sling based apps) `error.log` file*

_Output_: thelogist will generate an output file for each log level is encounters. Special care is taken for the error stack traces which are grouped together to also provide a count of how many times a specific error has been found in the logs.

How to build
------------
Running `mvn package` from the project's root will produce a distribution archive (`thelogist-*-dist.zip`) containing everything you need

```bash
$ mvn package
$ ll target/thelogist-*
$ .... target/thelogist-0.0.1-SNAPSHOT-dist.zip
$ .... target/thelogist-0.0.1-SNAPSHOT.jar
```

Or look under the [releases tab](../../releases) on github.

We use [Travis CI](http://travis-ci.org/) to verify the build: [![Build Status](https://travis-ci.org/stillalex/thelogist.svg?branch=master)](https://travis-ci.org/stillalex/thelogist)

We use [Coveralls](https://coveralls.io/r/stillalex/thelogist) for code coverage results: [![Coverage Status](https://coveralls.io/repos/stillalex/thelogist/badge.svg)](https://coveralls.io/r/stillalex/thelogist)


How to run
----------
```bash
$ java -jar thelogist-*.jar --path error.log [--config thelogist.conf --name test]
```

*Options*
* _--path_        path to the error log file (required)
* _--config_      path to the `thelogist.conf` file, not required but useful for defining ignores
* _--name_        appends the name to the output log files, not required but useful when parsing more than a single error log in the same location

Ignores
-------
 Ignores can be defined via the config file

```bash
logist.ignore {
    info {                          # log level
      OsgiInstallerImpl = ["*"]     # thread name = ignores are verified as 'contains' clauses, '*' means ignore all
      "*" = ["test.test.test.test"] # matches any thread name, ignore '*' means ignore all log entries
    }
}
```

Merging output logs
-------------------
 Usually each log level has its own dedicated file. This behavior is configurable via the config file, so one could map multiple log levels into a single output file

```bash
logist.output {
    info = "info"        # "info" level will to into the 'info' file
    debug = "info"       # "debug" level will also go into the 'info' file
}
```

Output file naming is based on the following recipe: `"log-" + (output mapping from level to fragment) + [name param] + ".txt"`, so you are free to use whatever fragment name you feel is representative.


Examples
--------

Given the following error log using the previously defined ignores:

`error.log`
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

* `log-info.txt`
```bash
01.01.2015,09:00:00.000,info,TestThread,"org.apache.sling.audit.osgi.installer Installed configuration"
```
_Note: the `OsgiInstallerImpl` log was ignored via the config._


* `log-warn.txt`
```bash
01.01.2015,09:00:00.000,warn,MapEntries Update,"org.apache.jackrabbit.oak.plugins.index.property.strategy.ContentMirrorStoreStrategy Traversed 132000 nodes using index sling:vanityPath with filter Filter(query=SELECT sling:vanityPath, sling:redirect, sling:redirectStatus FROM sling:VanityPath WHERE sling:vanityPath IS NOT NULL ORDER BY sling:vanityOrder DESC, path=*, property=[sling:vanityPath=])"
```

* `log-merge-error.txt`
```bash
[2] java.lang.NullPointerException: null
    at org.apache.sling.discovery.impl.common.heartbeat.HeartbeatHandler.issueClusterLocalHeartbeat(HeartbeatHandler.java:295)
```

License
-------

Copyright 2015 Alex Parvulescu.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
