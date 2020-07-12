# Results using G1GC

Using the following options:

```
javaOptions in Universal ++= Seq(
  "-J-Xms4G",
  "-J-Xmx4G",
  "-J-Xlog:gc*,gc+age=trace,gc+heap=debug,gc+promotion=trace,safepoint:file=/mnt/tmpfs/gc.log:utctime,pid,tags:filecount=10,filesize=1m",
  "-J-XX:MaxInlineLevel=18",
  "-J-XX:MaxInlineSize=270",
  "-J-XX:MaxTrivialSize=12",
  "-J-XX:-UseBiasedLocking",
  "-J-XX:+UnlockExperimentalVMOptions",
  "-J-XX:+UseJVMCICompiler",
  "-J-XX:StartFlightRecording:disk=true,filename=/mnt/tmpfs/memalloctest.jfr,maxage=10m,settings=profile"
),

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings",
  "-opt:l:inline",
  "-opt-inline-from:akka.**,com.lightbend.**,v1.**",
  "-opt-warnings:any-inline-failed",
)
```

TLAB allocation is around 1.25 GB/sec after the initial JVMCI spike.