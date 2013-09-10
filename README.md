# OPOWER Connection Pool Homework

This repository contains the implementation of OPOWER ConnectionPool (https://github.com/opower/connection_pool_scaffold).

## Thoughts

When implementing the connection pool, I decided to create a connection pool that blocks until a connection becomes available.  This requires a bit more complex synchronization.  It is key to acquire and release synchronization objects in the order that they are used.  Just in case a consumer wanted needed a method that got a connection and would time out if none were available there is a getConnectio(timeout, timeunit) method is available.

On the creation of the BlockingConnectionPool all connections are created. Connections that are closed will be reconnected when a consumer requests the connection.

Since this is a ConnectionPool only defines that it will be used for the interface Connection.  This means our connection pool should work for any Connection type.  The ConnectionWrapper class allows any DataSource to be used for this BlockingConnectionPool.  Adding this class also makes UT much easier.


## How To Running


    mvn compile      # compiles your code in src/main/java
    mvn test-compile # compile test code in src/test/java
    mvn test         # run tests in src/test/java for files named Test*.java


[maven]:http://maven.apache.org/

