import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.nio.Keys._

val staticVersionBuild = "0.4.4"
val versionBuild = scala.util.Properties.envOrElse("DISCOVERY_VERSION", staticVersionBuild)

val generateSWDiscoveryVersionFile = taskKey[Unit]("Generate SWDiscovery version file")

lazy val sttpClient4Version = "4.0.8"
lazy val lihaoyiUtestVersion = "0.8.5"
lazy val lihaoyiUpickleVersion = "4.2.1"
lazy val airframeLogVersion = "2025.1.12"
lazy val scalaUriVersion = "4.0.3"

lazy val scalaJsDomVersion = "2.1.0"
lazy val scalaJsMacrotaskExecutorVersion = "1.1.1"

lazy val npmComunicaVersion = "4.3.0"
lazy val npmComunicaBindingsFactoryVersion = "4.1.0"
lazy val npmN3Version = "1.26.0"
lazy val npmRdfxmlStreamingParserVersion = "3.0.1"

lazy val npmTypesNodeVersion = "18.11.18"
lazy val npmTypesSaxVersion = "latest"
lazy val npmTypesQsVersion = "latest"
lazy val npmTypesShowdownVersion = "latest"
lazy val npmTypesCombinedStreamVersion = "latest"
lazy val npmTypesMimeTypesVersion = "latest"
lazy val npmTypesJestVersion = "latest"
lazy val npmAxiosVersion = "latest"
lazy val npmBufferVersion = "latest"
lazy val npmShowdownVersion = "latest"
lazy val npmTypescriptVersion = "latest"
lazy val jestVersion = "latest"
lazy val tsJestVersion = "latest"

generateSWDiscoveryVersionFile := {
  val file =
    baseDirectory.value / "src" / "main" / "scala" / "fr" / "inrae" / "metabohub" / "semantic_web" / "SWDiscoveryVersionAtBuildTime.scala"

  if (!file.exists()) {
    IO.write(
      file,
      s"""|package fr.inrae.metabohub.semantic_web
          |
          |object SWDiscoveryVersionAtBuildTime {
          |  val version: String = "build ${java.time.LocalDate.now.toString}"
          |}
          |""".stripMargin
    )
  }
}

ThisBuild / organization := "com.github.p2m2"
ThisBuild / organizationName := "p2m2"
ThisBuild / name := "discovery"
ThisBuild / version := versionBuild
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organizationHomepage := Some(url("https://www6.inrae.fr/p2m2"))
ThisBuild / licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))
ThisBuild / homepage := Some(url("https://github.com/p2m2/discovery"))
ThisBuild / description := "Ease Sparql request to reach semantic database."
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/p2m2/discovery"),
    "scm:git@github.com:p2m2/Discovery.git"
  )
)
ThisBuild / developers := List(
  Developer("ofilangi", "Olivier Filangi", "olivier.filangi@inrae.fr", url("https://github.com/ofilangi"))
)

ThisBuild / credentials += {
  val realm = scala.util.Properties.envOrElse("REALM_CREDENTIAL", "")
  val host = scala.util.Properties.envOrElse("HOST_CREDENTIAL", "")
  val login = scala.util.Properties.envOrElse("LOGIN_CREDENTIAL", "")
  val pass = scala.util.Properties.envOrElse("PASSWORD_CREDENTIAL", "")
  val fileCredential = Path.userHome / ".sbt" / ".credentials"
  if (fileCredential.exists) Credentials(fileCredential)
  else Credentials(realm, host, login, pass)
}

ThisBuild / publishTo := {
  if (isSnapshot.value)
    Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")
  else
    Some("Sonatype Staging Nexus" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}

ThisBuild / publishConfiguration := publishConfiguration.value.withOverwrite(true)
ThisBuild / publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishMavenStyle := true

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, ScalablyTypedConverterPlugin)
  .settings(
    publish / skip := true,
    Compile / compile := (Compile / compile).dependsOn(generateSWDiscoveryVersionFile).value,

    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client4" %%% "core" % sttpClient4Version,
      "com.lihaoyi" %%% "utest" % lihaoyiUtestVersion % Test,
      "com.lihaoyi" %%% "upickle" % lihaoyiUpickleVersion,
      "org.wvlet.airframe" %%% "airframe-log" % airframeLogVersion,
      "io.lemonlabs" %%% "scala-uri" % scalaUriVersion,
      ("org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0").cross(CrossVersion.for3Use2_13),
      "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
      "org.scala-js" %%% "scala-js-macrotask-executor" % scalaJsMacrotaskExecutorVersion
    ),

    testFrameworks += new TestFramework("utest.runner.Framework"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-P:scalajs:nowarnGlobalExecutionContext"
    ),

    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.AllLibraryJars,
    Test / parallelExecution := false,
    coverageMinimumStmtTotal := 93,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,

    webpackBundlingMode := BundlingMode.LibraryAndApplication(),

    Compile / npmDependencies ++= Seq(
      "@types/node" -> npmTypesNodeVersion,
      "@types/sax" -> npmTypesSaxVersion,
      "@types/qs" -> npmTypesQsVersion,
      "@types/showdown" -> npmTypesShowdownVersion,
      "@types/combined-stream" -> npmTypesCombinedStreamVersion,
      "@types/mime-types" -> npmTypesMimeTypesVersion,
      "axios" -> npmAxiosVersion,
      "showdown" -> npmShowdownVersion,
      "n3" -> npmN3Version,
      "@comunica/query-sparql" -> npmComunicaVersion,
      "@comunica/utils-bindings-factory" -> npmComunicaBindingsFactoryVersion,
      "rdfxml-streaming-parser" -> npmRdfxmlStreamingParserVersion,
      "buffer" -> npmBufferVersion,
      "typescript" -> npmTypescriptVersion
    ),

    Compile / fastOptJS / scalaJSLinkerConfig ~= {
      _.withOptimizer(false)
        .withPrettyPrint(true)
        .withSourceMap(true)
    },

    Compile / fullOptJS / scalaJSLinkerConfig ~= {
      _.withSourceMap(false)
        .withModuleKind(ModuleKind.CommonJSModule)
    },

    stIgnore ++= List("asynciterator", "graphql")
  )

lazy val npmPackageJson = taskKey[Unit]("Build the discovery package.json")

npmPackageJson := {
  val scalaVersionBinary = scalaBinaryVersion.value
  val pkgFile = file(s"target/scala-$scalaVersionBinary/scalajs-bundler/main/package.json")
  val lines = IO.readLines(pkgFile).filter(_.nonEmpty)

  val indexStartDependencies = lines.zipWithIndex.collectFirst {
    case (v, i) if v.contains("dependencies") => i
  }.getOrElse(-1)

  val indexEndDependencies = lines.zipWithIndex.collectFirst {
    case (v, i) if v.contains("}") && i > indexStartDependencies => i
  }.getOrElse(-1)

  val dependencies = lines.zipWithIndex.collect {
    case (x, idx) if idx > indexStartDependencies && idx < indexEndDependencies => x
  }

  val out =
    file("package.json")

  IO.write(
    out,
    s"""|{
        |  "name": "@${(ThisBuild / organizationName).value}/${(ThisBuild / name).value}",
        |  "description": "${(ThisBuild / description).value}",
        |  "version": "${(ThisBuild / version).value}",
        |  "main": "./target/scala-$scalaVersionBinary/scalajs-bundler/main/discovery-opt.js",
        |  "types": "./ts/types/discovery.d.ts",
        |  "files": [
        |    "target/scala-$scalaVersionBinary/scalajs-bundler/main/discovery-opt.js"
        |  ],
        |  "scripts": {
        |    "test": "jest --detectOpenHandles"
        |  },
        |  "devDependencies": {
        |    "@types/jest": "^$npmTypesJestVersion",
        |    "jest": "^$jestVersion",
        |    "ts-jest": "^$tsJestVersion"
        |  },
        |  "jest": {
        |    "transform": {
        |      ".(ts|tsx)": "ts-jest"
        |    },
        |    "testRegex": "(ts/__tests__/.*|\\\\.(test|spec))\\\\.(ts|tsx|js)$$",
        |    "moduleFileExtensions": ["ts", "tsx", "js"]
        |  },
        |  "dependencies": {
        |${dependencies.mkString("\n")}
        |  },
        |  "repository": {
        |    "type": "git",
        |    "url": "git+https://github.com/p2m2/discovery.git"
        |  },
        |  "keywords": ["sparql", "rdf", "scalajs"],
        |  "author": "Olivier Filangi",
        |  "license": "MIT",
        |  "bugs": {
        |    "url": "https://github.com/p2m2/discovery/issues"
        |  },
        |  "homepage": "https://p2m2.github.io/discovery/"
        |}
        |""".stripMargin
  )
}

Global / onChangedBuildSource := ReloadOnSourceChanges
