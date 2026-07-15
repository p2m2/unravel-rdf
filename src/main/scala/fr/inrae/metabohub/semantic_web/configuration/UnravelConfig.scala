// SPDX-FileCopyrightText: 2020-2026 INRAE
// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * Discovery configuration definition.
 *
 *
 *
 */
package fr.inrae.metabohub.semantic_web.configuration

import fr.inrae.metabohub.semantic_web.exception._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.{Failure, Success}

@JSExportTopLevel(name="UnravelConfig")
object UnravelConfig {
  implicit val rw: OptionPickler.ReadWriter[UnravelConfig] = OptionPickler.macroRW

  /**
   * set a config using string configuration
   * @param json_conf : configuration in json format
   */
  @JSExport
  def setConfigString(json_conf: String) : UnravelConfig = {
    util.Try(OptionPickler.read[UnravelConfig](json_conf))
    match {
      case Success(v) => v
      case Failure(e) => throw SWStatementConfigurationException(e.getMessage)
    }
  }

  @JSExport
  def init() : UnravelConfig = UnravelConfig(
    settings = GeneralSetting(),
    sources = Seq(),
    proxy = None
  )

  @JSExport
  def proxy(urlProxy : String , method : String = "post" ) : UnravelConfig = {
    UnravelConfig(
      settings = GeneralSetting(),
      sources = Seq(),
      proxy = Some(ProxyConfiguration(urlProxy,method))
    )
  }
}

case class UnravelConfig(
                                     settings : GeneralSetting = new GeneralSetting(),
                                     sources : Seq[Source] = Seq(),
                                     proxy : Option[ProxyConfiguration] = None
                                   ) {

  @JSExport
  def source(idName : String) : Source = {
    sources.find(source => source.id == idName ) match {
      case Some(v : Source) => v
      case None => throw SWStatementConfigurationException("Unknown source id:"+idName )
    }
  }

  @JSExport
  def urlFile(
               filename : String ,
               mimetype : String = "text/turtle",
               method: String    = "POST",
               auth : String     = "",
               login : String    = "" ,
               password : String = "",
               token : String    = "") : UnravelConfig = _url(filename,mimetype,method,auth,login,password,token)

  @JSExport
  def sparqlEndpoint( url : String,
                      method: String    = "POST",
                      auth : String     = "",
                      login : String    = "" ,
                      password : String = "",
                      token : String    = "" ) : UnravelConfig =
    _url(url,"application/sparql-query",method,auth,login,password,token)

  // Not managed for node/js
  //@JSExport
  def localFile(
                 filename : String ,
                 mimetype : String = "text/turtle") : UnravelConfig =
    UnravelConfig(
      settings,
      sources :+ Source(
        id=filename,
        path=filename,
        content="",
        sourcePath=SourcePath.LocalFile,
        mimetype = mimetype
      ),
      proxy
    )
/* "content/rdf-xml" |  "content/turtle" |
           "content/n3" | "content/n-triples" |
           "content/ld+json" */
  @JSExport
  def rdfContent(
                  content : String ,
                  mimetype : String = "content/turtle") : UnravelConfig =
    UnravelConfig(
      settings,
      sources :+ Source(
        id=content.hashCode.toString,
        path="",
        content=content,
        sourcePath=SourcePath.Content,
        mimetype = mimetype
      ),
      proxy
    )

  @JSExport
  def sourcesSize: Int = sources.length

  @JSExport
  def setPageSize(pageSize : Int) : UnravelConfig = UnravelConfig(
    settings.copy(pageSize=pageSize),
    sources,proxy)

  @JSExport
  def pageSize: Int  = settings.pageSize

  @JSExport
  def setSizeBatchProcessing(sizeBatchProcessing : Int)  : UnravelConfig = UnravelConfig(
    settings.copy(sizeBatchProcessing=sizeBatchProcessing),
    sources,proxy)

  @JSExport
  def sizeBatchProcessing: Int  = settings.sizeBatchProcessing

  @JSExport
  def setLogLevel(logLevel : String) : UnravelConfig = UnravelConfig(
    settings.copy(logLevel=logLevel),
    sources,proxy)

  @JSExport
  def logLevel: String = settings.logLevel

  @JSExport
  def setCache(cache : Boolean) : UnravelConfig = UnravelConfig(
    settings.copy(cache=cache),
    sources,proxy)

  @JSExport
  def cache: Boolean = settings.cache

  private[this] def _url(
                          path : String ,
                          mimetype : String = "",
                          method: String    = "POST",
                          auth : String     = "",
                          login : String    = "" ,
                          password : String = "",
                          token : String    = "") : UnravelConfig =
    UnravelConfig(
      settings,
      sources :+ Source(
        id=path,
        path=path,
        content="",
        sourcePath=SourcePath.UrlPath,
        mimetype=mimetype,
        method=Some(method),
        auth= auth match {
          case "" => None
          case v => Some(v)
        },
        login= login match {
          case "" => None
          case v => Some(v)
        },
        password= password match {
          case "" => None
          case v => Some(v)
        },
        token= token match {
          case "" => None
          case v => Some(v)
        }),proxy
    )
}
