# README

Sample Play Project using JDK 14 features.

This is here mostly to back up [Logging vs Memory](https://tersesystems.com/blog/2020/07/09/logging-vs-memory/) blog post.

## Requirements

Run this with SDKMAN or Jabba to ensure you have JDK 14 across the board.  

```
sdk use java 14.0.1.hs-adpt
```

## Mount

```bash
sudo su -
mkdir -p /mnt/tmpfs
mount -t tmpfs tmpfs /mnt/tmpfs
```

## Building

```bash
sbt ";clean; stage"
```

## Running


Start the server with:

```bash
./run-stage.sh
```

```bash
sbt "gatling / gatling:test"
```
 
Then go to "JVM Internals" / TLAB Allocations to see what the allocation rate is.  Ideally it should be less than 1 GB/sec.

## JMC / Java Flight Recorder

The flight recorder options are in [profile](https://blogs.oracle.com/javamagazine/java-flight-recorder-and-jfr-event-streaming-in-java-14) mode.  

The JFR file is written out to the tmpfs mount.

```
cd /mnt/tmpfs/
file memalloctest.jfr
```

You can load up the JFR in [Java Mission Control](https://adoptopenjdk.net/jmc.html).

You can also attach JMC to a running Play process.  It should say `play.core.server.ProdServerStart`.

## Interpreting Java Flight Recorder

Java Flight Recorder doesn't show TLAB allocations per second.  It shows allocations per interval, which by default is 15 seconds!

This means you have to zoom in with the mousewheel to get the appropriate graph to see the rate per second.

## Using ZGC with Large Pages

Following the [guide](https://wiki.openjdk.java.net/display/zgc/Main#Main-EnablingLargePagesOnLinux):

We're using a 4 GB heap, so we need 2048 large pages.

```
sudo su - 
echo 2048 > /sys/kernel/mm/hugepages/hugepages-2048kB/nr_hugepages
mkdir /hugepages

# my uid is 1000
mount -t hugetlbfs -o uid=1000 nodev /hugepages 
```