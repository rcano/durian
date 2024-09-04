val scala3Version = "3.5.0"

inThisBuild(
  Seq(
    organization := "octad",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )
)

lazy val root = project
  .dependsOn(macros)
  .in(file("."))
  .settings(
    name := "durian",
    scalacOptions += "-explain",
  )

lazy val macros = project.settings(name := "durian-macros")
