package myscalalib

// This file was generated using sn-bindgen 0.4.4: https://sn-bindgen.indoorvivants.com/

import _root_.scala.scalanative.unsafe.*
import _root_.scala.scalanative.unsigned.*
import _root_.scala.scalanative.libc.*
import _root_.scala.scalanative.*

object predef:
  private[myscalalib] trait _BindgenEnumCUnsignedInt[T](using
      eq: T =:= CUnsignedInt
  ):
    given Tag[T] = Tag.UInt.asInstanceOf[Tag[T]]
    extension (inline t: T)
      inline def value: CUnsignedInt = t.asInstanceOf[CUnsignedInt]
      inline def int: CInt           = value.toInt
      inline def uint: CUnsignedInt  = value

object enumerations:
  import predef.*
  opaque type myscalalib_operation = CUnsignedInt
  object myscalalib_operation
      extends _BindgenEnumCUnsignedInt[myscalalib_operation]:
    given _tag: Tag[myscalalib_operation]                    = Tag.UInt
    inline def define(inline a: Long): myscalalib_operation  = a.toUInt
    val MULTIPLY                                             = define(1)
    val ADD                                                  = define(2)
    def getName(value: myscalalib_operation): Option[String] =
      value match
        case `MULTIPLY` => Some("MULTIPLY")
        case `ADD`      => Some("ADD")
        case _          => _root_.scala.None
    extension (a: myscalalib_operation)
      inline def &(b: myscalalib_operation): myscalalib_operation = a & b
      inline def |(b: myscalalib_operation): myscalalib_operation = a | b
      inline def is(b: myscalalib_operation): Boolean             = (a & b) == b
  end myscalalib_operation
end enumerations

object structs:
  import _root_.myscalalib.predef.*
  import _root_.myscalalib.enumerations.*
  import _root_.myscalalib.structs.*

  opaque type myscalalib_config = CStruct2[myscalalib_operation, CString]

  object myscalalib_config:
    given _tag: Tag[myscalalib_config] =
      Tag.materializeCStruct2Tag[myscalalib_operation, CString]

    export fields.*
    private[myscalalib] object fields:
      extension (struct: myscalalib_config)
        inline def op: myscalalib_operation                = struct._1
        inline def op_=(value: myscalalib_operation): Unit =
          (!struct.at1 = value)
        inline def label: CString                = struct._2
        inline def label_=(value: CString): Unit = (!struct.at2 = value)
      end extension

    // Allocates myscalalib_config on the heap – fields are not initalised or zeroed out
    def apply()(using Zone): Ptr[myscalalib_config] =
      scala.scalanative.unsafe.alloc[myscalalib_config](1)
    def apply(op: myscalalib_operation, label: CString)(using
        Zone
    ): Ptr[myscalalib_config] =
      val ____ptr = apply()
      (!____ptr).op = op
      (!____ptr).label = label
      ____ptr
  end myscalalib_config
end structs

trait ExportedFunctions:
  import _root_.myscalalib.predef.*
  import _root_.myscalalib.enumerations.*
  import _root_.myscalalib.structs.*
  def myscalalib_run(
      config: Ptr[myscalalib_config],
      left: Float,
      right: Float
  ): Float

object functions extends ExportedFunctions:
  import _root_.myscalalib.predef.*
  import _root_.myscalalib.enumerations.*
  import _root_.myscalalib.structs.*
  @exported
  override def myscalalib_run(
      config: Ptr[myscalalib_config],
      left: Float,
      right: Float
  ): Float = myscalalib.impl.Implementations.myscalalib_run(config, left, right)
end functions

object types:
  export _root_.myscalalib.structs.*
  export _root_.myscalalib.enumerations.*

object all:
  export _root_.myscalalib.enumerations.myscalalib_operation
  export _root_.myscalalib.structs.myscalalib_config
  export _root_.myscalalib.functions.myscalalib_run
