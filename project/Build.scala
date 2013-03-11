import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "seqserver"
  val appVersion      = "0.1.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    "org.apache.hadoop" % "hadoop-client" % "2.0.0-cdh4.2.0"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Cloudera Repos" at "https://repository.cloudera.com/artifactory/cloudera-repos/"
  )

}
