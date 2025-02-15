package myscalalib.impl

import scalanative.unsafe.*
import myscalalib.all.*

object Implementations extends myscalalib.ExportedFunctions:

  def myscalalib_run(
      config: Ptr[myscalalib_config],
      left: Float,
      right: Float
  ): Float =
    val cfg = !config
    val label = fromCString(cfg.label)
    if cfg.op == myscalalib_operation.ADD then
      println(s"[$label] $left + $right = ${left + right}")
      left + right
    else if cfg.op == myscalalib_operation.MULTIPLY then
      println(s"[$label] $left * $right = ${left * right}")
      left * right
    else ???
