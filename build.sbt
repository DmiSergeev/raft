import SbtCommons._
import sbtcrossproject.crossProject
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

name := "raft"

scalacOptions in Compile ++= Seq("-Ypartial-unification", "-Xdisable-assertions")

javaOptions in Test ++= Seq("-ea")

commons

enablePlugins(AutomateHeaderPlugin)

lazy val `core` = project.in(file("raft-core"))