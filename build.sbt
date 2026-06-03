import org.scalajs.jsenv.nodejs.NodeJSEnv
import sbt.*
import sbt.Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*

import scala.sys.process.Process

val staticVersionBuild = "local.build"
val versionBuild = scala.util.Properties.envOrElse("DISCOVERY_VERSION", staticVersionBuild)

val generateSWDiscoveryVersionFile = taskKey[Unit]("Generate SWDiscovery version file")
val npmPrepareRelease = taskKey[File]("Prepare an optimized npm publication directory in target/npm")
val npmPrepareDebugRelease = taskKey[File]("Prepare a debug npm publication directory in target/npm-debug")
val cdnPrepare = taskKey[File]("Build a browser-ready UMD bundle in target/cdn")


lazy val lihaoyiUtestVersion = "0.9.5"
lazy val lihaoyiUpickleVersion = "4.4.3"
lazy val airframeLogVersion = "2025.1.12"
lazy val scalaUriVersion = "4.0.3"
lazy val scalaJsDomVersion = "2.1.0"
lazy val scalaJsMacrotaskExecutorVersion = "1.1.1"

lazy val npmAxiosVersion = "1.16.1"
lazy val npmShowdownVersion = "2.1.0"
lazy val npmComunicaVersion = "4.5.0"
lazy val npmN3Version = "1.26.0"
lazy val npmRdfxmlStreamingParserVersion = "3.0.1"
lazy val npmTypesNodeVersion = "18.11.18"
lazy val npmTypescriptVersion = "6.0.3"
lazy val npmQsVersion = "6.15.2"

def bundledArtifact(base: File, scalaBinary: String, projectName: String, optimized: Boolean): File = {
  val suffix = if (optimized) "opt" else "fastopt"
  base / "target" / s"scala-$scalaBinary" / "scalajs-bundler" / "main" / s"$projectName-$suffix.js"
}

def bundledSourceMap(base: File, scalaBinary: String, projectName: String): File =
  base / "target" / s"scala-$scalaBinary" / "scalajs-bundler" / "main" / s"$projectName-fastopt.js.map"

def renderPackageJson(
                       packageName: String,
                       description: String,
                       version: String,
                       mainFile: String,
                       includedFiles: Seq[String],
                       dependencies: Seq[(String, String)],
                       registryUrl: String,
                       cdnFile: Option[String] = None
                     ): String = {

  val cdnFields = cdnFile.map { f =>
    s"""  "browser": "$f",
       |  "unpkg": "$f",
       |  "jsdelivr": "$f",""".stripMargin
  }.getOrElse("")

  val dependenciesJson =
    if (dependencies.isEmpty) ""
    else dependencies.map { case (dep, ver) =>
      val safeVer = ver.replace("\"", "\\\"")
      s"""    "$dep": "$safeVer""""
    }.mkString(",\n")

  val filesJson =
    includedFiles.map(f => s"""    "$f"""").mkString(",\n")

  s"""{
     |  "name": "$packageName",
     |  "description": "$description",
     |  "version": "$version",
     |  "main": "$mainFile",
     |$cdnFields
     |  "files": [
     |$filesJson
     |  ],
     |  "repository": {
     |    "type": "git",
     |    "url": "https://forge.inrae.fr/p2m2/unravel-rdf.git"
     |  },
     |  "keywords": [
     |    "sparql",
     |    "rdf",
     |    "scalajs",
     |    "semantic-web",
     |    "metabolomics"
     |  ],
     |  "author": "Olivier Filangi",
     |  "license": "MIT",
     |  "bugs": {
     |    "url": "https://forge.inrae.fr/p2m2/unravel-rdf/-/issues"
     |  },
     |  "homepage": "https://forge.inrae.fr/p2m2/unravel-rdf",
     |  "publishConfig": {
     |    "registry": "$registryUrl"
     |  },
     |  "dependencies": {
     |$dependenciesJson
     |  }
     |}
     |""".stripMargin
}

def prepareNpmDir(
                   base: File,
                   scalaBinary: String,
                   projectName: String,
                   orgName: String,
                   projectVersion: String,
                   projectDescription: String,
                   npmDeps: Seq[(String, String)],
                   optimized: Boolean,
                   outputDirName: String,
                   log: Logger,
                   cdnFile: Option[String] = None
                 ): File = {
  val bundledJs = bundledArtifact(base, scalaBinary, projectName, optimized)
  val sourceMap = bundledSourceMap(base, scalaBinary, projectName)
  val npmDir = base / "target" / outputDirName
  val outputJs = npmDir / s"$projectName.js"
  val outputPackageJson = npmDir / "package.json"
  val registryUrl = s"https://forge.inrae.fr/api/v4/projects/${sys.env.getOrElse("CI_PROJECT_ID", "unravel-rdf")}/packages/npm/"

  if (!bundledJs.exists())
    sys.error(s"Missing bundled artifact: ${bundledJs.getAbsolutePath}. Run the appropriate Scala.js task first.")

  IO.delete(npmDir)
  IO.createDirectory(npmDir)
  IO.copyFile(bundledJs, outputJs)

  var includedFiles = Seq(s"$projectName.js")

  if (!optimized && sourceMap.exists()) {
    IO.copyFile(sourceMap, npmDir / s"$projectName.js.map")
    includedFiles = includedFiles :+ s"$projectName.js.map"
  }

  val packageJsonContent = renderPackageJson(
    packageName = s"@$orgName/$projectName",
    description = projectDescription,
    version = projectVersion,
    mainFile = s"./$projectName.js",
    includedFiles = includedFiles,
    dependencies = npmDeps,
    registryUrl = registryUrl,
    cdnFile = cdnFile
  )

  IO.write(outputPackageJson, packageJsonContent)
  log.info(s"Prepared npm package in ${npmDir.getAbsolutePath}")
  npmDir
}

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
name := "unravel-rdf"
version := versionBuild
scalaVersion := "2.13.18"
organizationHomepage := Some(url("https://www6.inrae.fr/p2m2"))
licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))
homepage := Some(url("https://forge.inrae.fr/p2m2/unravel-rdf"))
description := "Unravel RDF graphs — interactive SPARQL session management with lazy pagination, serialization, and graph traversal."

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
      "typescript" -> npmTypescriptVersion,
      "qs" -> npmQsVersion
    ),

    Test / npmDependencies ++= Seq(
      "axios" -> npmAxiosVersion,
      "showdown" -> npmShowdownVersion,
      "@comunica/query-sparql" -> npmComunicaVersion,
      "n3" -> npmN3Version,
      "rdfxml-streaming-parser" -> npmRdfxmlStreamingParserVersion,
      "@types/node" -> npmTypesNodeVersion,
      "typescript" -> npmTypescriptVersion,
      "qs" -> npmQsVersion
    ),

    Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withOptimizer(false).withPrettyPrint(true).withSourceMap(true) },

    Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false).withModuleKind(ModuleKind.CommonJSModule) },

    npmPrepareRelease := prepareNpmDir(
      base = baseDirectory.value,
      scalaBinary = scalaBinaryVersion.value,
      projectName = name.value,
      orgName = organizationName.value,
      projectVersion = version.value,
      projectDescription = description.value,
      npmDeps = (Compile / npmDependencies).value,
      optimized = true,
      outputDirName = "npm",
      log = streams.value.log,
      cdnFile = Some(s"./${name.value}.js")
    ),

    npmPrepareDebugRelease := prepareNpmDir(
      base = baseDirectory.value,
      scalaBinary = scalaBinaryVersion.value,
      projectName = name.value,
      orgName = organizationName.value,
      projectVersion = version.value,
      projectDescription = description.value,
      npmDeps = (Compile / npmDependencies).value,
      optimized = false,
      outputDirName = "npm-debug",
      log = streams.value.log
    ),

    cdnPrepare := {
      val npmDir = npmPrepareRelease.value  // génère target/npm/ avec package.json + unravel-rdf.js
      val cdnDir = baseDirectory.value / "target" / "cdn"
      val webpackConfig = (baseDirectory.value / "webpack.cdn.config.js").getAbsolutePath
      val log = streams.value.log

      // npm install dans target/npm/ — isole les deps du scope test
      log.info(s"Installing npm dependencies in ${npmDir.getAbsolutePath}...")
      val installResult = Process("npm install", npmDir).!(log)
      if (installResult != 0) sys.error("npm install failed in target/npm")

      // webpack depuis target/npm/ — node_modules local résolu automatiquement
      log.info("Running webpack CDN bundle...")
      val bundleResult = Process(
        Seq("npx", "webpack-cli", "--config", webpackConfig, "--no-cache"),
        npmDir
      ).!(log)
      if (bundleResult != 0) sys.error("webpack CDN bundle failed")

      log.info(s"CDN bundle ready in ${cdnDir.getAbsolutePath}")
      cdnDir
    }
  )

Global / onChangedBuildSource := ReloadOnSourceChanges