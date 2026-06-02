import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

val staticVersionBuild = "0.4.4"
val versionBuild = scala.util.Properties.envOrElse("DISCOVERY_VERSION", staticVersionBuild)

val generateSWDiscoveryVersionFile = taskKey[Unit]("Generate SWDiscovery version file")
val npmPrepareRelease = taskKey[File]("Prepare an optimized npm publication directory in target/npm")
val npmPrepareDebugRelease = taskKey[File]("Prepare a debug npm publication directory in target/npm-debug")

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

def bundledArtifact(base: File, scalaBinary: String, projectName: String, optimized: Boolean): File = {
  val suffix = if (optimized) "opt" else "fastopt"
  base / "target" / s"scala-$scalaBinary" / "scalajs-bundler" / "main" / s"${projectName}-$suffix.js"
}

def bundledSourceMap(base: File, scalaBinary: String, projectName: String): File =
  base / "target" / s"scala-$scalaBinary" / "scalajs-bundler" / "main" / s"${projectName}-fastopt.js.map"

def renderPackageJson(
                       packageName: String,
                       description: String,
                       version: String,
                       mainFile: String,
                       includedFiles: Seq[String],
                       dependencies: Seq[(String, String)],
                       registryUrl: String
                     ): String = {
  val dependenciesJson =
    if (dependencies.isEmpty) ""
    else dependencies.map { case (dep, ver) => s"""    "${dep}": "${ver}""" }.mkString(",\n")

  val filesJson =
    includedFiles.map(f => s"""    "${f}"""").mkString(",\n")

  s"""{
     |  "name": "$packageName",
     |  "description": "$description",
     |  "version": "$version",
     |  "main": "$mainFile",
     |  "files": [
     |$filesJson
     |  ],
     |  "repository": {
     |    "type": "git",
     |    "url": "https://forge.inrae.fr/p2m2/discovery.git"
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
     |    "url": "https://forge.inrae.fr/p2m2/discovery/-/issues"
     |  },
     |  "homepage": "https://forge.inrae.fr/p2m2/discovery",
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
                   log: Logger
                 ): File = {
  val bundledJs = bundledArtifact(base, scalaBinary, projectName, optimized)
  val sourceMap = bundledSourceMap(base, scalaBinary, projectName)
  val npmDir = base / "target" / outputDirName
  val outputJs = npmDir / s"${projectName}.js"
  val outputPackageJson = npmDir / "package.json"
  val readmeFile = base / "README.md"
  val registryUrl = s"https://forge.inrae.fr/api/v4/projects/${sys.env.getOrElse("CI_PROJECT_ID", "YOUR_PROJECT_ID")}/packages/npm/"

  if (!bundledJs.exists())
    sys.error(s"Missing bundled artifact: ${bundledJs.getAbsolutePath}. Run the appropriate Scala.js task first.")

  IO.delete(npmDir)
  IO.createDirectory(npmDir)
  IO.copyFile(bundledJs, outputJs)

  var includedFiles = Seq(s"${projectName}.js")

  if (!optimized && sourceMap.exists()) {
    IO.copyFile(sourceMap, npmDir / s"${projectName}.js.map")
    includedFiles = includedFiles :+ s"${projectName}.js.map"
  }

  if (readmeFile.exists()) {
    IO.copyFile(readmeFile, npmDir / "README.md")
    includedFiles = includedFiles :+ "README.md"
  }

  val packageJsonContent = renderPackageJson(
    packageName = s"@${orgName}/${projectName}",
    description = projectDescription,
    version = projectVersion,
    mainFile = s"./${projectName}.js",
    includedFiles = includedFiles,
    dependencies = npmDeps,
    registryUrl = registryUrl
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
      log = streams.value.log
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
    )
  )

Global / onChangedBuildSource := ReloadOnSourceChanges