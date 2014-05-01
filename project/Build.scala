import sbt._
import Keys._

object DolphinBuild extends Build {
  import BuildSettings._
  import Dependencies._

  val resolutionRepos = Seq(
    "maven" at "http://repo1.maven.org/maven2/",
    "Twitter Maven Repo" at "http://maven.twttr.com/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )

  lazy val parent = Project(id = "dolphin",
    base = file("."))
    .aggregate (client, manager, common)
    .settings(basicSettings: _*)

  lazy val common = Project(id = "common", base = file("common"))
    .settings(basicSettings: _*)
    .settings(libraryDependencies ++=
    compile(logback) ++
      test(scalaTest))

  lazy val client = Project(id = "client", base = file("client"))
    .settings(basicSettings: _*)
    .settings(libraryDependencies ++=
    compile(akka_actor, akka_remote, akka_slf4j) ++
      test(scalaTest, akka_testkit))
    .dependsOn(common)

  lazy val broker = Project(id = "broker", base = file("broker"))
    .settings(basicSettings: _*)
    .settings(libraryDependencies ++=
    compile(akka_actor, akka_remote, akka_slf4j) ++
      test(scalaTest, akka_testkit))
    .dependsOn(common)

  lazy val manager = Project(id = "manager", base = file("manager"))
    .settings(basicSettings: _*)
    .settings(libraryDependencies ++=
      compile(akka_actor, akka_remote, akka_slf4j, quartz) ++
      test(scalaTest, akka_testkit))
    .dependsOn(common)
}
