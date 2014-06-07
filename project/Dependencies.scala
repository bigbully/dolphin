import sbt._

object Dependencies {

  val resolutionRepos = Seq(
//    "Twitter Maven Repo" at "http://maven.twttr.com/",
    "maven" at "http://repo1.maven.org/maven2/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  lazy val scalaTest =  "org.scalatest" %% "scalatest" % "2.0.RC1"
  lazy val akka_actor = "com.typesafe.akka" %% "akka-actor" % "2.3.2"
  lazy val akka_slf4j = "com.typesafe.akka" %% "akka-slf4j" % "2.3.2"
  lazy val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % "2.3.2"
  lazy val akka_remote  = "com.typesafe.akka" %% "akka-remote" % "2.3.2"
  lazy val quartz = "org.quartz-scheduler" % "quartz" % "2.2.1"
  //lazy val netty = "io.netty" % "netty-all" % "4.0.18.Final"

  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.0.7"
}