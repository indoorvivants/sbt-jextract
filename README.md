# sbt-jextract

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
