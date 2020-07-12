import sbt.Keys._

val AkkaVersion = "2.6.7"

initialize := {
  val _        = initialize.value // run the previous initialization
  val required = "14"
  val current  = sys.props("java.specification.version")
  assert(current >= required, s"Unsupported JDK: java.specification.version $current != $required")
}

val graalOptions: Seq[String] = Seq(
  "-J-XX:+UnlockExperimentalVMOptions",
  "-J-XX:+UseJVMCICompiler"
)

val shenOptions: Seq[String] = Seq(
  "-J-XX:+UnlockExperimentalVMOptions",
  "-J-XX:+UseShenandoahGC",
  "-J-XX:ShenandoahGCHeuristics=compact"
)

val zgcOptions: Seq[String] = Seq(
  "-J-XX:+UnlockExperimentalVMOptions",
  "-J-XX:+UseZGC",
)

lazy val root = (project in file("."))
  .enablePlugins(PlayService, PlayLayoutPlugin, Common)
  .settings(
    name := "memory-allocation-test",
    scalaVersion := "2.13.3",
    resolvers += Resolver.bintrayRepo("tersesystems", "maven"),
    resolvers += "clojars" at "https://repo.clojars.org/",
    // https://github.com/mcculls/guice-betas/ to stop the jdk 11 reflective access warning
    resolvers += "guice-betas" at "https://mcculls.github.io/guice-betas/maven2/",
    libraryDependencies ++= Seq(
      guice,
      "org.joda" % "joda-convert" % "2.2.1",
      "com.tersesystems.blindsight" %% "blindsight-logstash" % "1.4.0-RC4",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.0",
      "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "io.lemonlabs" %% "scala-uri" % "1.5.1",
      "net.codingwell" %% "scala-guice" % "4.2.6",
      "nl.grons" %% "metrics4-scala" % "4.1.9",
      "com.google.inject" % "guice" % "4.2.4-20200419-NEWAOP-BETA", // fixes https://github.com/google/guice/issues/1133
      "com.clojure-goes-fast" % "jvm-alloc-rate-meter" % "0.1.3",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
    ),
    javaOptions in Universal ++= Seq(
      "-J-Xms4G",
      "-J-Xmx4G",
      "-J-Xlog:gc*,gc+age=trace,gc+heap=debug,gc+promotion=trace,safepoint:file=/mnt/tmpfs/gc.log:utctime,pid,tags:filecount=10,filesize=1m",
      "-J-XX:MaxInlineLevel=18",
      "-J-XX:MaxInlineSize=270",
      "-J-XX:MaxTrivialSize=12",
      "-J-XX:-UseBiasedLocking",
      "-J-XX:+AlwaysPreTouch",      
      "-J-XX:StartFlightRecording:disk=true,filename=/mnt/tmpfs/memalloctest.jfr,maxage=10m,settings=profile"
    ) ++ shenOptions,

    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings",
      "-opt:l:inline",
      "-opt-inline-from:akka.**,com.lightbend.**,v1.**",
      "-opt-warnings:any-inline-failed",
    )
  )

lazy val gatlingVersion = "3.3.1"
lazy val gatling = (project in file("gatling"))
  .enablePlugins(GatlingPlugin)
  .settings(
    scalaVersion := "2.12.10",
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % Test,
      "io.gatling" % "gatling-test-framework" % gatlingVersion % Test
    )
  )
