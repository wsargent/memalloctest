import sbt.Keys._

val AkkaVersion = "2.6.7"

val graalOptions: Seq[String] = Seq(
  "-J-XX:+UnlockExperimentalVMOptions",
  "-J-XX:+UseJVMCICompiler"
)

val shenOptions: Seq[String] = Seq(
  "-J-XX:+UnlockExperimentalVMOptions",
  "-J-XX:+UseShenandoahGC",
  "-J-XX:-ShenandoahPacing",
  "-J-XX:ShenandoahGCHeuristics=compact"
)

val g1gcOptions = Seq("-J-XX:+UseG1GC")

val jvmOptions = Seq(
  "-J-Xms4G",
  "-J-Xmx4G",
  "-J-XX:MaxInlineLevel=18",
  "-J-XX:MaxInlineSize=270",
  "-J-XX:MaxTrivialSize=12",
  "-J-XX:-UseBiasedLocking",
  "-J-XX:+AlwaysPreTouch",
  "-J-XX:+UseNUMA"
) ++ g1gcOptions

val jfrOptions = 
  "-XX:StartFlightRecording:disk=true,filename=${LOG_DIR}/memalloctest.jfr,maxage=10m,settings=profile"

val heapDumpOptions = 
 "-XX:HeapDumpPath=$LOG_DIR/heapdump.hprof"    

val gclog18Options = Seq(
  "-XX:+PrintGC",
  "-XX:+PrintGCApplicationStoppedTime",
  "-XX:+PrintGCDateStamps",
  "-XX:+PrintGCDetails",
  "-XX:+PrintGCTimeStamps",
  "-XX:+PrintTenuringDistribution",
  "-Xloggc:${LOG_DIR}/gc.log"
)

val gclog9Options =
  "-Xlog:gc*,gc+age=debug,gc+heap=debug,gc+promotion=debug,safepoint:file=${LOG_DIR}/gc.log:utctime,pid,tags:filecount=10,filesize=1m"

// Create a docker image with sbt docker:publishLocal
lazy val root = (project in file("."))
  .enablePlugins(PlayService, PlayLayoutPlugin, DockerPlugin, Common)
  .settings(
    name := "memory-allocation-test",
    scalaVersion := "2.13.3",

    // disable javadoc/scaladoc generation
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,

    resolvers += Resolver.bintrayRepo("tersesystems", "maven"),
    resolvers += "clojars" at "https://repo.clojars.org/",
    // https://github.com/mcculls/guice-betas/ to stop the jdk 11 reflective access warning
    resolvers += "guice-betas" at "https://mcculls.github.io/guice-betas/maven2/",

    dockerExposedPorts ++= Seq(9000),

    // Point the Play logs at the right place.
    Docker / defaultLinuxLogsLocation := (Docker / defaultLinuxInstallLocation).value + "/logs",
    dockerExposedVolumes := Seq((Docker / defaultLinuxLogsLocation).value),

    // Always use latest tag
    dockerUpdateLatest := true,

    // Use image that has been stripped down
    dockerBaseImage := "adoptopenjdk/openjdk14:slim",

    // Set the log directory if we are staging not in docker
    bashScriptExtraDefines += "export LOG_DIR=${app_home}/../logs",
    bashScriptExtraDefines += "mkdir -p $LOG_DIR",
    bashScriptExtraDefines += "echo LOG_DIR=$LOG_DIR",
    
    // Don't write out a PID file, it doesn't matter anyway.    
    bashScriptExtraDefines += """addJava "-Dpidfile.path=/dev/null"""",
    bashScriptExtraDefines += """addJava "-Dplay.http.secret.key=a-long-secret-to-defeat-entropy"""",
    //bashScriptExtraDefines ++= gclog18Options.map(gcOption => s"""addJava "${gcOption}""""),
    bashScriptExtraDefines += s"""addJava "${gclog9Options}"""",
    // bashScriptExtraDefines += s"""addJava "$jfrOptions""""" + ,

    // Expose LOG_DIR as environment variable in Docker.
    dockerEnvVars := Map(
      "LOG_DIR" -> (Docker / defaultLinuxLogsLocation).value
    ),

    libraryDependencies ++= Seq(
      // logging
      "com.tersesystems.blindsight" %% "blindsight-logstash" % "1.4.0-RC4",

      // Akka's jackson has an old version here that must be upgraded
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.0",

      // Play 2.8.x doesn't always have latest version of Akka
      "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,

      // Some guice add-ons for easy Play use
      guice,
      "net.codingwell" %% "scala-guice" % "4.2.6",
      "com.google.inject" % "guice" % "4.2.4-20200419-NEWAOP-BETA", // fixes https://github.com/google/guice/issues/1133

      // Scala API for dropwizard-metrics
      "nl.grons" %% "metrics4-scala" % "4.1.9",

      // ???
      "org.joda" % "joda-convert" % "2.2.1",

      // Expose the allocation rate meter
      // This uses getThreadAllocatedBytes, it may be faster to use JEP-331 agent
      "com.clojure-goes-fast" % "jvm-alloc-rate-meter" % "0.1.3",

      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
    ),

    javaOptions in Universal ++= jvmOptions,

    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings",
      "-opt:l:inline",
      "-opt-inline-from:akka.**,com.lightbend.**,v1.**",
      "-opt-warnings:any-inline-failed",
    )
  )
