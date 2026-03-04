val scala3Version = "3.8.2"

inThisBuild(
  Seq(
    organization := "org.octad",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "durian",
    scalacOptions ++= Seq("-explain", "-preview", "-experimental"),
  )
