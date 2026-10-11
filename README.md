# sbt-jextract

[![sbt-jextract Scala version support](https://index.scala-lang.org/indoorvivants/sbt-jextract/sbt-jextract/latest-by-scala-version.svg?platform=sbt1)](https://index.scala-lang.org/indoorvivants/sbt-jextract/sbt-jextract)

A SBT plugin that wraps [jextract](https://github.com/openjdk/jextract) to generate 
[FFM](https://docs.oracle.com/en/java/javase/23/core/foreign-function-and-memory-api.html#GUID-E7255CE9-5A95-437C-B37A-276B6C9B5F4D) bindings from header files.

Plugin's API and behaviour is designed similarly to [sn-bindgen](https://sn-bindgen.indoorvivants.com/), and the plugin can bootstrap jextract automatically.


## Installation

**project/plugins.sbt**

```scala
addSbtPlugin("com.indoorvivants" % "sbt-jextract" % "<version>")
```

**build.sbt**

Add this to a Java/Scala project that needs some FFM bindings:

```scala
.enablePlugins(JextractPlugin)
.settings(
  jextractBindings += JextractBinding(
    (ThisBuild / baseDirectory).value / "interface.h",
    "myscalalib_bindings"
  ),
  jextractMode := JextractMode.ResourceGenerator, // default, only mentioned for documentation purposes
)
```

Adjust the location of the header and `jextractMode` value to your preference.

`withArgs` passes extra arguments to jextract itself, and `withJavaOptions` passes options to the JVM jextract runs in (through the `JEXTRACT_JAVA_OPTIONS` variable its launcher reads). For example, jextract splits a large header class into a chain of classes every 1000 declarations, which Scala then has to import one by one, since it doesn't see a Java class's inherited statics; raising that limit keeps it to one class:

```scala
jextractBindings += JextractBinding(
  (ThisBuild / baseDirectory).value / "interface.h",
  "myscalalib_bindings"
).withJavaOptions(Seq("-Djextract.decls.per.header=100000"))
```
