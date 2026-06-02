import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

val staticVersionBuild = "0.4.4"
val versionBuild = scala.util.Properties.envOrElse("DISCOVERY_VERSION", staticVersionBuild)

val generateSWDiscoveryVersionFile = taskKey[Unit]("Generate SWDiscovery version file")

lazy val lihaoyiUtestVersion = "0.8.5"
lazy val lihaoyiUpickleVersion = "4.2.1"
lazy val airframeLogVersion = "2025.1.12"
lazy val scalaUriVersion = "4.0.3"
lazy val scalaJsDomVersion = "2.1.0"
lazy val scalaJsMacrotaskExecutorVersion = "1.1.1"

lazy val npmAxiosVersion = "1.8.4"
lazy val npmShowdownVersion = "2.1.0"
lazy val npmComunicaVersion = "4.3.0"
lazy val npmN3Version = "1.26.0"
lazy val npmRdfxmlStreamingParserVersion = "3.0.1"
lazy val npmTypesNodeVersion = "18.11.18"
lazy val npmTypescriptVersion = "6.0.3"

generateSWDiscoveryVersionFile := {
  val file =
    baseDirectory.value / "src" / "main" / "scala" / "fr" / "inrae" / "metabohub" / "semantic_web" / "SWDiscoveryVersionAtBuildTime.scala"

  IO.write(
    file,
    s"""|package fr.inrae.metabohub.semantic_web
        |
        |object SWDiscoveryVersionAtBuildTime {
        |  val version: String = "${version.value}"
        |}
        |""".stripMargin
  )
}

organization := "fr.inrae.metabohub.p2m2"
organizationName := "p2m2"
name := "discovery"
version := versionBuild
scalaVersion := "2.13.16"
organizationHomepage := Some(url("https://www6.inrae.fr/p2m2"))
licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))
homepage := Some(url("https://forge.inrae.fr/p2m2/discovery"))
description := "Ease SPARQL requests to semantic databases."

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    useYarn := false,

    testFrameworks += new TestFramework("utest.runner.Framework"),

    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-P:scalajs:nowarnGlobalExecutionContext"
    ),

    Test / parallelExecution := false,

    webpackBundlingMode := BundlingMode.LibraryOnly(),

    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % lihaoyiUpickleVersion,
      "io.lemonlabs" %%% "scala-uri" % scalaUriVersion,
      "org.wvlet.airframe" %%% "airframe-log" % airframeLogVersion,
      "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
      "org.scala-js" %%% "scala-js-macrotask-executor" % scalaJsMacrotaskExecutorVersion,
      "org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0",
      "com.lihaoyi" %%% "utest" % lihaoyiUtestVersion % Test
    ),

    Compile / npmDependencies ++= Seq(
      "axios" -> npmAxiosVersion,
      "showdown" -> npmShowdownVersion,
      "@comunica/query-sparql" -> npmComunicaVersion,
      "n3" -> npmN3Version,
      "rdfxml-streaming-parser" -> npmRdfxmlStreamingParserVersion,
      "@types/node" -> npmTypesNodeVersion,
      "typescript" -> npmTypescriptVersion
    ),

    Test / npmDependencies ++= Seq(
      "axios" -> npmAxiosVersion,
      "showdown" -> npmShowdownVersion,
      "@comunica/query-sparql" -> npmComunicaVersion,
      "n3" -> npmN3Version,
      "rdfxml-streaming-parser" -> npmRdfxmlStreamingParserVersion,
      "@types/node" -> npmTypesNodeVersion,
      "typescript" -> npmTypescriptVersion
    ),

    Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withOptimizer(false).withPrettyPrint(true).withSourceMap(true) },

    Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false).withModuleKind(ModuleKind.CommonJSModule) }
  )

Global / onChangedBuildSource := ReloadOnSourceChanges