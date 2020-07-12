import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

/**
  * Settings that are common to all the SBT projects
  */
object Common extends AutoPlugin {
  override def trigger = allRequirements
  override def requires: sbt.Plugins = JvmPlugin

  override def projectSettings = Seq(
    organization := "com.tersesystems",
    version := "1.0-SNAPSHOT",
    javacOptions ++= Seq("--release", "14"),
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8"
    )
  )
}
