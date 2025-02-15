addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")

// Code quality
//addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"    % "0.4.2")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.12.1")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.10.0")


addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.6")
addSbtPlugin("com.indoorvivants" % "bindgen-sbt-plugin" % "0.2.3")

// Compiled documentation
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.5.1")

libraryDependencies ++= List(
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value,
  "com.indoorvivants.detective" %% "platform" % "0.1.0"
)

Compile / unmanagedSourceDirectories +=
  (ThisBuild / baseDirectory).value.getParentFile /
    "mod" / "sbt-plugin" / "src" / "main" / "scala"
