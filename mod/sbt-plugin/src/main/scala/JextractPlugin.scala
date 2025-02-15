package sbt_jextract

import com.indoorvivants.detective.Platform
import sbt.*
import sbt.Keys.*
import sjsonnew.JsonFormat

import java.io.{FileOutputStream, InputStream}
import scala.util.Try
import java.security.*;
import com.indoorvivants.detective.Platform.OS.Linux
import com.indoorvivants.detective.Platform.OS.MacOS
import com.indoorvivants.detective.Platform.OS.Unknown
import com.indoorvivants.detective.Platform.OS.Windows
import com.indoorvivants.detective.Platform.Arch.Arm
import com.indoorvivants.detective.Platform.Arch.Intel

/** Mode of operation for jextract integration.
  *
  * JextractMode.ResourceGenerator will generate bindings in an opaque location
  * managed by SBT It's great for iteration on the header files, as any
  * compilation will trigger the generator automatically. This of course
  * requires having jextract (and more importantly LLVM's libclang, because this
  * plugin can bootstrap jextract, but not LLVM) installed locally.
  *
  * JextractMode.Manual(dir) requires manually invoking `jextractGenerate`, and
  * is designed for bindings that you check into your codebase and update
  * rarely.
  */
sealed trait JextractMode extends Product with Serializable
object JextractMode {
  case object ResourceGenerator    extends JextractMode
  case class Manual(javaDir: File) extends JextractMode
}

class JextractBinding private (args: JextractArgsImpl) {

  private def copy(f: JextractArgsImpl => JextractArgsImpl) =
    new JextractBinding(
      f(args)
    )

  /** Set the additional arguments passed to jextract. This method will
    * overwrite the current value with the supplied one.
    *
    * @param args
    * @return
    */
  def withArgs(args: Seq[String]) = copy(_.copy(extra = args.toList))

  def toCLIArguments(outputFolder: Option[File]): Seq[String] = {
    val b = Seq.newBuilder[String]

    outputFolder.foreach { outputFolder =>
      b += "--output"
      b += outputFolder.toPath().toAbsolutePath().toString()
    }

    b += "--target-package"
    b += args.pkg

    b ++= args.extra

    b += args.file.toPath().toAbsolutePath().toString()

    b.result()
  }

  private[sbt_jextract] def getArgs = args

  override def toString(): String =
    s"JextractBinding[jextract ${toCLIArguments(None).mkString(" ")}]"
}

object JextractBinding {
  def apply(inputFile: File, packageName: String) =
    new JextractBinding(
      JextractArgsImpl(file = inputFile, pkg = packageName)
    )
}

private case class JextractArgsImpl(
    file: File,
    pkg: String,
    extra: List[String] = List.empty
)

object JextractPlugin extends AutoPlugin {

  object autoImport {
    val jextractBinary   = taskKey[File]("Path to jextract binary")
    val jextractGenerate = taskKey[Seq[File]]("Generate all jextract bindings")
    val jextractBindings =
      taskKey[Seq[JextractBinding]]("List of configured jextract bindings")
    val jextractMode = settingKey[JextractMode](
      "Mode (resource generator or manually invoked) to run jextract in"
    )
  }

  private val jextractDownloadUrl = settingKey[String](
    s"Download link to jextract's .tar.gz distribution"
  )

  private val jextractVersion = settingKey[JextractVersion]("")

  import autoImport.*

  private val resolveBinaryTask =
    Def.task[Try[File]] {
      val res = (dependencyResolution).value

      def download(link: String, to: java.nio.file.Path, report: Int => Unit) =
        Try {
          val url           = new URI(link).toURL
          val conn          = url.openConnection()
          val contentLength = conn.getContentLength()
          var is            = Option.empty[InputStream]
          var out           = Option(new FileOutputStream(to.toFile))
          try {
            val inputStream = conn.getInputStream()
            is = Some(inputStream)
            var downloaded = 0
            val buffer     = Array.ofDim[Byte](16384)
            var length     = 0

            // Looping until server finishes
            var percentage = 0
            while ({ length = inputStream.read(buffer); length } != -1) {
              // Writing data
              out.foreach(_.write(buffer, 0, length))
              downloaded += length
              val newPercentage = (downloaded * 100) / contentLength
              if (newPercentage != percentage) {
                report(newPercentage)
                percentage = newPercentage
              }

            }
          } finally {
            is.foreach(_.close())
            out.foreach(_.close())
          }
        }

      val link = jextractDownloadUrl.value
      val md   = MessageDigest.getInstance("MD5")
      md.update(link.getBytes())
      val hash =
        String.format("%032x", new java.math.BigInteger(1, md.digest()))

      val cacheDir =
        (ThisBuild / streams).value.cacheDirectory / s"v_$hash"
      cacheDir.mkdirs()

      val version = jextractVersion.value

      val binPath = cacheDir / s"jextract-${version.base}/bin/jextract"

      def check(path: File) =
        path.exists() && path.canExecute()

      if (check(binPath))
        Try(binPath)
      else {
        val archiveDestination = cacheDir / "jextract.tar.gz"
        sLog.value.info(
          s"Downloading jextract from [$link] to [$archiveDestination]"
        )

        download(
          link,
          archiveDestination.toPath(), {
            var prev = 0
            i =>
              if (i - prev >= 10) {
                prev = i
                sLog.value.info(s"Downloaded ${i}%")
              }
          }
        )

        import scala.sys.process.*

        s"tar -zvxf $archiveDestination -C $cacheDir".!!

        assert(
          check(binPath),
          s"After extracting the archive, [$binPath] still doesn't look like a normal binary - something is wrong"
        )

        Try(binPath)
      }
    }

  override def projectSettings: Seq[Setting[_]] = Seq(
    jextractDownloadUrl := jextractVersion.value.toURL(Platform.target),
    jextractBinary      := resolveBinaryTask.value.get,
    jextractVersion     := JextractVersion(22, 6, 47),
    jextractMode        := JextractMode.ResourceGenerator,
    jextractBindings    := Seq.empty
  ) ++
    Seq(Compile, Test).flatMap(conf => inConfig(conf)(definedSettings(conf)))

  private def targetFolder(sourceManaged: File) =
    sourceManaged / "jextract_generated"

  private def definedSettings(addConf: Configuration) = Seq(
    unmanagedSourceDirectories ++= {
      if (jextractMode.value == JextractMode.ResourceGenerator)
        Seq(targetFolder(sourceManaged.value))
      else Seq.empty
    },
    sourceGenerators ++= {
      if (jextractMode.value.isInstanceOf[JextractMode.Manual]) Seq.empty
      else Seq(jextractGenerate.taskValue)
    },
    jextractGenerate := {
      val selected = (addConf / jextractBindings).value

      val managedDestination = targetFolder(sourceManaged.value)

      val dest = jextractMode.value match {
        case JextractMode.ResourceGenerator => managedDestination
        case JextractMode.Manual(javaDir)   => javaDir
      }

      val binary = jextractBinary.value
      val log    = sLog.value

      def runOne(b: JextractBinding) = {
        val stderr = List.newBuilder[String]
        val stdout = List.newBuilder[String]

        import scala.sys.process.ProcessLogger

        val logger = ProcessLogger.apply(
          (o: String) => stdout += o,
          (e: String) => stderr += e
        )
        val args = b.toCLIArguments(Some(dest))
        val cmd  = binary.toPath.toString +: args

        log.info(s"Executing jextract command [$args]")

        def errPrintln(s: String) = System.err.println(s)

        val process = new java.lang.ProcessBuilder(cmd*)
          .start()

        scala.io.Source
          .fromInputStream(process.getErrorStream())
          .getLines()
          .foreach(errPrintln(_))

        scala.io.Source
          .fromInputStream(process.getInputStream())
          .getLines()
          .foreach(logger.out(_))

        val result = process.waitFor()

        IO.listFiles(dest / b.getArgs.pkg).toSet
      }

      incremental(binary, bindings = selected, runOne(_), streams.value)
    }
  )

  private def incremental(
      binary: File,
      bindings: Seq[JextractBinding],
      run: JextractBinding => Set[File],
      streams: TaskStreams
  ) = {
    import sjsonnew.*, BasicJsonProtocol.*
    implicit val configFormat: JsonFormat[JextractArgsImpl] =
      caseClassArray(JextractArgsImpl.apply _, JextractArgsImpl.unapply _)

    case class Input(
        binary: FilesInfo[HashFileInfo],
        inputs: FilesInfo[HashFileInfo],
        configs: Seq[JextractArgsImpl]
    )

    implicit val inputFormat: JsonFormat[Input] =
      caseClassArray(Input.apply _, Input.unapply _)

    val cacheFile =
      streams.cacheDirectory

    val tracker = Tracked.inputChanged[Input, Set[File]](cacheFile / "input") {
      (changed: Boolean, in: Input) =>
        Tracked.diffOutputs(cacheFile / "output", FileInfo.exists) {
          (outDiff: ChangeReport[File]) =>
            if (changed || outDiff.modified.nonEmpty) {
              bindings.flatMap(run).toSet
            } else outDiff.checked
        }
    }

    val inputs: FilesInfo[HashFileInfo] =
      FileInfo.hash(bindings.map(_.getArgs.file).toSet)

    val binaryInput: FilesInfo[HashFileInfo] =
      FileInfo.hash(Set(binary))

    tracker(Input(binaryInput, inputs, bindings.map(_.getArgs))).toSeq

  }
}

private case class JextractVersion(base: Int, something: Int, build: Int) {
  def toURL(platform: Platform.Target) = {
    val os =
      platform.os match {
        case Linux   => "linux"
        case MacOS   => "macos"
        case Windows => "windows"
      }

    import Platform.Arch.*, Platform.Bits
    val abi = (platform.arch, platform.bits) match {
      case (Intel, Bits.x64) => "x64"
      case (Intel, Bits.x32) => "x32"
      case (Arm, Bits.x64)   => "aarch64"
      case (Arm, Bits.x32)   => "aarch32"
    }

    s"https://download.java.net/java/early_access/jextract/$base/$something/openjdk-$base-jextract+$something-${build}_$os-${abi}_bin.tar.gz"
  }
}
