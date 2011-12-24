All plugin JARs should be dropped into this directory.  If they implement the
Java Service Provider [1] specification, they will be automatically, dynamically
loaded.  If they do not, they may be specified in the configuration file, in
a plugin element.

[1] http://java.sun.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider
