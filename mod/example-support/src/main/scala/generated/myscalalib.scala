package myscalalib

import _root_.scala.scalanative.unsafe.*
import _root_.scala.scalanative.unsigned.*
import _root_.scala.scalanative.libc.*
import _root_.scala.scalanative.*

object predef:
  private[myscalalib] trait _BindgenEnumCUnsignedInt[T](using eq: T =:= CUnsignedInt):
    given Tag[T] = Tag.UInt.asInstanceOf[Tag[T]]
    extension (inline t: T)
     inline def value: CUnsignedInt = eq.apply(t)
     inline def int: CInt = eq.apply(t).toInt
     inline def uint: CUnsignedInt = eq.apply(t)


object enumerations:
  import predef.*
  opaque type myscalalib_operation = CUnsignedInt
  object myscalalib_operation extends _BindgenEnumCUnsignedInt[myscalalib_operation]:
    given _tag: Tag[myscalalib_operation] = Tag.UInt
    inline def define(inline a: Long): myscalalib_operation = a.toUInt
    val MULTIPLY = define(1)
    val ADD = define(2)
    inline def getName(inline value: myscalalib_operation): Option[String] =
      inline value match
        case MULTIPLY => Some("MULTIPLY")
        case ADD => Some("ADD")
        case _ => _root_.scala.None
    extension (a: myscalalib_operation)
      inline def &(b: myscalalib_operation): myscalalib_operation = a & b
      inline def |(b: myscalalib_operation): myscalalib_operation = a | b
      inline def is(b: myscalalib_operation): Boolean = (a & b) == b

object structs:
  import _root_.myscalalib.enumerations.*
  import _root_.myscalalib.predef.*
  import _root_.myscalalib.structs.*
  opaque type myscalalib_config = CStruct2[myscalalib_operation, CString]
  object myscalalib_config:
    given _tag: Tag[myscalalib_config] = Tag.materializeCStruct2Tag[myscalalib_operation, CString]
    def apply()(using Zone): Ptr[myscalalib_config] = scala.scalanative.unsafe.alloc[myscalalib_config](1)
    def apply(op : myscalalib_operation, label : CString)(using Zone): Ptr[myscalalib_config] = 
      val ____ptr = apply()
      (!____ptr).op = op
      (!____ptr).label = label
      ____ptr
    extension (struct: myscalalib_config)
      def op : myscalalib_operation = struct._1
      def op_=(value: myscalalib_operation): Unit = !struct.at1 = value
      def label : CString = struct._2
      def label_=(value: CString): Unit = !struct.at2 = value

trait ExportedFunctions:
  import _root_.myscalalib.enumerations.*
  import _root_.myscalalib.predef.*
  import _root_.myscalalib.structs.*
  def myscalalib_run(config : Ptr[myscalalib_config], left : Float, right : Float): Float


object functions extends ExportedFunctions:
  import _root_.myscalalib.enumerations.*
  import _root_.myscalalib.predef.*
  import _root_.myscalalib.structs.*
  @exported
  override def myscalalib_run(config : Ptr[myscalalib_config], left : Float, right : Float): Float = myscalalib.impl.Implementations.myscalalib_run(config, left, right)

object types:
  export _root_.myscalalib.structs.*
  export _root_.myscalalib.enumerations.*

object all:
  export _root_.myscalalib.enumerations.myscalalib_operation
  export _root_.myscalalib.structs.myscalalib_config