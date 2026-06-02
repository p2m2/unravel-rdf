package fr.inrae.metabohub.data

import scala.scalajs.js

object NodeEnv {
  def get(key: String, default: String): String = {
    val processEnv = js.Dynamic.global.process.env
    val value = processEnv.selectDynamic(key)
    if (js.isUndefined(value)) default else value.asInstanceOf[String]
  }
}