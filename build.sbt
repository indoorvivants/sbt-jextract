inThisBuild(
  List(
    organization        := "com.indoorvivants",
    sonatypeProfileName := "com.indoorvivants",
    organizationName    := "Anton Sviridov",
    homepage := Some(
      url("https://github.com/indoorvivants/sbt-jextract")
    ),
    startYear := Some(2025),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "keynmol",
        "Anton Sviridov",
        "keynmol@gmail.com",
        url("https://blog.indoorvivants.com")
      )
    )
  )
)

organization        := "com.indoorvivants"
sonatypeProfileName := "com.indoorvivants"

lazy val plugin = project
  .in(file("mod/sbt-plugin"))
  .settings(
    sbtPlugin                     := true,
    pluginCrossBuild / sbtVersion := "1.10.7",
    libraryDependencies += "com.indoorvivants.detective" %% "platform" % "0.1.0",
    moduleName := "sbt-jextract",
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scalacOptions += "-Ywarn-unused-import",
    scriptedBufferLog := false
  )
  .enablePlugins(ScriptedPlugin, SbtPlugin)

import sbt_jextract.*

lazy val example = project
  .in(file("mod/example"))
  // example usage starts
  .enablePlugins(JextractPlugin)
  .settings(
    jextractBindings += JextractBinding(
      (ThisBuild / baseDirectory).value / "mod/example/interface.h",
      "myscalalib_bindings"
    ),
    jextractMode := JextractMode.ResourceGenerator,
  )
  // example usage ends
  // below is just build stuff
  .settings(
    publish / skip := true,
    publishLocal / skip := true,
    run / fork := true,
    javaOptions += "--enable-native-access=ALL-UNNAMED",
    run / envVars += ("SCALA_NATIVE_LIB" -> (LocalProject("exampleSupport") / Compile / nativeLink).value.toString)
  )

import bindgen.interface.*
import bindgen.plugin.*
import scalanative.build.*

lazy val exampleSupport = project
  .in(file("mod/example-support"))
  .enablePlugins(ScalaNativePlugin, BindgenPlugin)
  .settings(
    publish / skip := true,
    publishLocal / skip := true,
    scalaVersion := "3.6.3",
    bindgenBindings :=
      Seq(
        Binding(
          (example / baseDirectory).value / "interface.h",
          "myscalalib"
        ).withExport(true)
      ),
    bindgenMode := BindgenMode.Manual(
      scalaDir = (Compile / sourceDirectory).value / "scala" / "generated",
      cDir = (Compile / resourceDirectory).value / "scala-native" / "generated"
    ),
    bindgenBindings := {
      bindgenBindings.value.map(_.withNoLocation(true))
    },
    nativeConfig ~= { _.withBuildTarget(BuildTarget.libraryDynamic) }
  )
