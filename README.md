# Memory Allocation Test

This project demos the performance and GC differences between JDK 1.8, 11, and 14.  It also shows Shenandoah performance considerations.

Initial explorations are from [Logging vs Memory](https://tersesystems.com/blog/2020/07/09/logging-vs-memory/) blog post.  You can read more at [Benchmarking Logging with JDK 14](https://tersesystems.com/blog/2020/08/23/benchmarking-logging-with-jdk-14/).

JDK management was done with [jabba](https://github.com/shyiko/jabba).

## Results

GC logs and Gatling results are in the results subdirectory.  

### JDK 1.8 with G1GC

* [gceasy](https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjAvMDgvMjQvLS1nYy5sb2ctLTE2LTUyLTA=&channel=WEB) /
* [gatling](https://refined-github-html-preview.kidonng.workers.dev/wsargent/memalloctest/raw/master/results/jdk1.8-g1gc/gatlingspec-20200824161655347/index.html)

### JDK 11 with G1GC

* [gceasy](https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjAvMDgvMjcvLS1nYy5sb2ctLTMtNDQtNTE=&channel=WEB) 
* [gatling](https://refined-github-html-preview.kidonng.workers.dev/wsargent/memalloctest/raw/master/results/jdk11-g1gc/gatlingspec-20200825021623878/index.html)

### JDK 14 with G1GC
 
* [gceasy](https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjAvMDgvMjcvLS1nYy5sb2ctLTQtMi0zNg==&channel=WEB) 
* [gatling](https://refined-github-html-preview.kidonng.workers.dev/wsargent/memalloctest/raw/master/results/jdk14-g1gc/gatlingspec-20200824203409837/index.html)

### JDK 14 with Shenandoah

* [gceasy](https://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMjAvMDgvMjcvLS1nYy5sb2ctLTQtMy0xMw==&channel=WEB) 
* [gatling](https://refined-github-html-preview.kidonng.workers.dev/wsargent/memalloctest/raw/master/results/jdk14-shenandoah/gatlingspec-20200824205013461/index.html)