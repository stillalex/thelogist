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
