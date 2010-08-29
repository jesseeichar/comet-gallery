import sbt._

class LiftProject(info: ProjectInfo) extends DefaultWebProject(info) with IdeaProject {
  val mavenLocal = "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"
  val scalatoolsSnapshot = "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/"
  val scalatoolsRelease = "Scala Tools Snapshot" at "http://scala-tools.org/repo-releases/"
  val liftVersion = "2.1-SNAPSHOT"

  override def managedStyle = ManagedStyle.Maven
  override def jettyWebappPath = webappPath
  override def scanDirectories = Nil
  override def jettyPort = 8090

  override def libraryDependencies = Set(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-util" % liftVersion % "compile->default" withSources (),
    "net.liftweb" %% "lift-common" % liftVersion % "compile->default" withSources (),
    "net.liftweb" %% "lift-testkit" % liftVersion % "compile->default",
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "test->default",
    "junit" % "junit" % "4.5" % "test->default",
    "org.scala-tools.testing" % "specs" % "1.6.1" % "test->default",
    "org.scala-lang" % "scalaio_2.8.0" % "0.1.0" % "compile->default"
    ) ++ super.libraryDependencies
}
