import sbt._
import Keys._

object BuildSettings {

  lazy val basicSettings = seq(
    version               := "0.1.0-SNAPSHOT",
    organization          := "org.dolphin",
    startYear             := Some(2014),
    licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaVersion          := "2.10.3",
    resolvers             ++= Dependencies.resolutionRepos
  )

}
