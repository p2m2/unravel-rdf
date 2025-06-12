import sbt.Keys._
import sbt._
import sbtcrossproject.CrossPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbtassembly.AssemblyPlugin.autoImport._
import sbt.nio.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

/* Discovery Configuration */
val static_version_build = "0.4.4"
val version_build = scala.util.Properties.envOrElse("DISCOVERY_VERSION", static_version_build)
val SWDiscoveryVersionAtBuildTimeFile = "./shared/src/main/scala/fr/inrae/metabohub/semantic_web/SWDiscoveryVersionAtBuildTime.scala"

/* Common */
lazy val sttp_client4_version = "4.0.8"
lazy val lihaoyi_utest_version = "0.8.5"
lazy val lihaoyi_upickle_version = "4.2.1"
lazy val airframe_log_version = "2025.1.12"
lazy val scala_uri_version = "4.0.3"

/* JVM */
lazy val lihaoyi_requests_version = "0.9.0"
lazy val scalajs_stubs_version = "1.1.0"
lazy val slf4j_version = "2.0.17"
lazy val rdf4j_version = "4.3.16"

/* JS packages */
lazy val scalajs_dom_version = "2.1.0"
lazy val scala_js_macrotask_executor = "1.1.1"

/* --- NPM Packages Versions --- */

lazy val npm_comunica_version = "4.2.0"
lazy val npm_comunica_bindings_factory = "3.3.0"
lazy val npm_n3="1.26.0"
lazy val npm_rdfxml_streaming_parser = "3.0.1"
lazy val npm_typescript_version = "latest"

lazy val npm_axios_version = "latest"
lazy val npm_buffer_version = "latest"
lazy val npm_showdown_version = "latest"
lazy val npm_types_jest = "latest"
lazy val npm_types_sax = "latest"
lazy val npm_types_qs = "latest"
lazy val npm_types_showdown = "latest"
lazy val npm_types_combined_stream = "latest"
lazy val npm_types_mime_types = "latest"
lazy val npm_types_node = "18.11.18"
lazy val jest = "latest"
lazy val tsjest = "latest"

/* --- Génération du fichier de version --- */
val generateSWDiscoveryVersionFile = taskKey[Unit]("SWDiscovery version file.")

generateSWDiscoveryVersionFile := {
  val file = baseDirectory.value / "shared" / "src" / "main" / "scala" / "fr" / "inrae" / "metabohub" / "semantic_web" / "SWDiscoveryVersionAtBuildTime.scala"
  if (!file.exists()) {
    IO.write(
      file,
      s"""|
         |package fr.inrae.metabohub.semantic_web
         |
         |object SWDiscoveryVersionAtBuildTime {
         |   val version : String = " build ${java.time.LocalDate.now.toString}"
         |}""".stripMargin
    )
  }
}

/* --- Paramètres globaux --- */
ThisBuild / organization := "com.github.p2m2"
ThisBuild / organizationName := "p2m2"
ThisBuild / name := "discovery"
ThisBuild / version := version_build
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
  val realm = scala.util.Properties.envOrElse("REALM_CREDENTIAL", "" )
  val host = scala.util.Properties.envOrElse("HOST_CREDENTIAL", "" )
  val login = scala.util.Properties.envOrElse("LOGIN_CREDENTIAL", "" )
  val pass = scala.util.Properties.envOrElse("PASSWORD_CREDENTIAL", "" )
  val file_credential = Path.userHome / ".sbt" / ".credentials"
  if (file_credential.exists) {
    Credentials(file_credential)
  } else {
    Credentials(realm, host, login, pass)
  }
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

/* --- Projet racine d'agrégation --- */
lazy val root = (project in file("."))
  .aggregate(discovery.js, discovery.jvm)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true,
    Compile / compile := (Compile / compile).dependsOn(generateSWDiscoveryVersionFile).value
  )

/* --- Projet cross-platform --- */
lazy val discovery = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .jsConfigure(
    _.enablePlugins(ScalaJSBundlerPlugin)
    .enablePlugins(ScalablyTypedConverterPlugin))
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client4" %%% "core" % sttp_client4_version,
      "com.lihaoyi" %%% "utest" % lihaoyi_utest_version % Test,
      "com.lihaoyi" %%% "upickle" % lihaoyi_upickle_version,
      "org.wvlet.airframe" %%% "airframe-log" % airframe_log_version,
      "io.lemonlabs" %%% "scala-uri" % scala_uri_version
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    scalacOptions ++= Seq("-deprecation", "-feature"),
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.AllLibraryJars,
    coverageMinimumStmtTotal := 93,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,
    Test / parallelExecution := false
  )
  .jsSettings(
    scalacOptions ++= Seq("-P:scalajs:nowarnGlobalExecutionContext"),
    libraryDependencies ++= Seq(
      (
        "org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0").cross(CrossVersion.for3Use2_13),
        "org.scala-js" %%% "scalajs-dom" % scalajs_dom_version,
        "org.scala-js" %%% "scala-js-macrotask-executor" % scala_js_macrotask_executor  
    ),
    webpackBundlingMode := BundlingMode.LibraryAndApplication(),
    Compile / npmDependencies ++= Seq(
      "@types/node" -> npm_types_node,
      "@types/sax" -> npm_types_sax,
      "@types/qs" -> npm_types_qs,
      "@types/showdown" -> npm_types_showdown, 
      "@types/combined-stream" -> npm_types_combined_stream,
      "@types/mime-types" -> npm_types_mime_types,
      "axios" -> npm_axios_version,
      "showdown" -> npm_showdown_version,
      "n3" -> npm_n3,
      "@comunica/query-sparql" ->  npm_comunica_version,
      "@comunica/bindings-factory" -> npm_comunica_bindings_factory,
      "rdfxml-streaming-parser" -> npm_rdfxml_streaming_parser,
      "buffer" -> npm_buffer_version,
      "typescript" -> npm_typescript_version
    ),
    Compile / fastOptJS / scalaJSLinkerConfig ~= {
      _.withOptimizer(false)
        .withPrettyPrint(true)
        .withSourceMap(true)
    },
    Compile / fullOptJS / scalaJSLinkerConfig ~= {
      _.withSourceMap(false)
        .withModuleKind(ModuleKind.CommonJSModule)
    }
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "requests" % lihaoyi_requests_version,
      "org.scala-js" %% "scalajs-stubs" % scalajs_stubs_version % "provided",
      "org.slf4j" % "slf4j-api" % slf4j_version,
      "org.slf4j" % "slf4j-simple" % slf4j_version,
      "org.eclipse.rdf4j" % "rdf4j-sail" % rdf4j_version,
      ("org.eclipse.rdf4j" % "rdf4j-storage" % rdf4j_version)
        .exclude("commons-codec", "commons-codec"),
      ("org.eclipse.rdf4j" % "rdf4j-tools-federation" % rdf4j_version)
        .exclude("commons-codec", "commons-codec")
    ),
    assembly / assemblyJarName := s"discovery-$version_build.jar",
    assembly / logLevel := Level.Info,
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case "module-info.class"      => MergeStrategy.first
      case x =>
        val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )


lazy val discoveryJS = discovery.js
  .settings(
    stIgnore ++= List("asynciterator","graphql")
  )

/* --- Génération package.json pour npm --- */
lazy val npmPackageJson = taskKey[Unit]("Build the discovery package.json")

npmPackageJson := {
  val scalaJsBundlerPackageJsonFile = IO.readLines(new File("js/target/scala-2.13/scalajs-bundler/main/package.json")).filter(_.nonEmpty)
  val indexStartDependencies = scalaJsBundlerPackageJsonFile.zipWithIndex.collectFirst {
    case (v, i) if v.contains("dependencies") => i
  }.getOrElse(-1)
  val indexEndDependencies = scalaJsBundlerPackageJsonFile.zipWithIndex.collectFirst {
    case (v, i) if v.contains("}") && i > indexStartDependencies => i
  }.getOrElse(-1)
  val dependencies = scalaJsBundlerPackageJsonFile.zipWithIndex.collect {
    case (x, idx) if idx > indexStartDependencies && idx < indexEndDependencies => x
  }
  reflect.io.File("./package.json").writeAll(
    s"""{
       "name": "@${(ThisBuild / organizationName).value}/${(ThisBuild / name).value}",
       "description": "${(ThisBuild / description).value}",
       "version": "${(ThisBuild / version).value}",
       "main": "./js/target/scala-2.13/scalajs-bundler/main/discovery-opt.js",
       "types": "./ts/types/discovery.d.ts",
       "files": [
         "js/target/scala-2.13/scalajs-bundler/main/discovery-opt.js"
       ],
       "scripts": {
        "test": "jest --detectOpenHandles"
        },
      "devDependencies": {
        "@types/jest": "^$npm_types_jest ",
        "jest": "^$jest ",
        "ts-jest": "^$tsjest"
      },
      "jest": {
        "transform": {
          ".(ts|tsx)": "ts-jest"
        },
        "testRegex": "(ts/__tests__/.*|\\\\.(test|spec))\\\\.(ts|tsx|js)$$",
        "moduleFileExtensions": [
          "ts",
          "tsx",
          "js"
        ]
       },
       "dependencies": {
    ${dependencies.mkString("\n")}
       },
       "repository": {
         "type": "git",
         "url": "git+https://github.com/p2m2/discovery.git"
       },
       "keywords": [
         "sparql",
         "rdf",
         "scalajs"
       ],
       "author": "Olivier Filangi",
       "license": "MIT",
       "bugs": {
         "url": "https://github.com/p2m2/discovery/issues"
       },
       "homepage": "https://p2m2.github.io/discovery/"
     }
     """.stripMargin
  )
}

Global / onChangedBuildSource := ReloadOnSourceChanges
