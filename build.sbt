import org.scalajs.jsenv.nodejs.NodeJSEnv
import sbt.*
import sbt.Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*
import scala.sys.process.Process
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

lazy val buildTime = java.time.LocalDateTime.now()
  .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))

val generateUnravelVersionFile = taskKey[Unit]("Generate Unravel version file")
val npmPrepareRelease = taskKey[File]("Prepare an optimized npm publication directory in target/npm")
val npmPrepareDebugRelease = taskKey[File]("Prepare a debug npm publication directory in target/npm-debug")
val cdnPrepare = taskKey[File]("Build a browser-ready UMD bundle in target/cdn")
val cdnDebugPrepare = taskKey[File]("Build a browser-ready debug bundle in target/cdn-debug")

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

lazy val npmWebpackVersion = "5.102.1"
lazy val npmWebpackCliVersion = "5.1.4"
lazy val npmSourceMapLoaderVersion = "5.0.0"

def bundledArtifact(base: File, scalaBinary: String, projectName: String, optimized: Boolean): File = {
  val suffix = if (optimized) "opt" else "fastopt"
  base / "target" / s"scala-$scalaBinary" / "scalajs-bundler" / "main" / s"$projectName-$suffix.js"
}

def bundledSourceMap(base: File, scalaBinary: String, projectName: String): File =
  base / "target" / s"scala-$scalaBinary" / "scalajs-bundler" / "main" / s"$projectName-fastopt.js.map"

def jsonEscape(s: String): String =
  s.replace("\\", "\\\\").replace("\"", "\\\"")

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
    s"""  "browser": "${jsonEscape(f)}",
       |  "unpkg": "${jsonEscape(f)}",
       |  "jsdelivr": "${jsonEscape(f)}",""".stripMargin
  }.getOrElse("")

  val dependenciesJson =
    if (dependencies.isEmpty) ""
    else dependencies.map { case (dep, ver) =>
      s"""    "${jsonEscape(dep)}": "${jsonEscape(ver)}""""
    }.mkString(",\n")

  val filesJson =
    includedFiles.map(f => s"""    "${jsonEscape(f)}"""").mkString(",\n")

  s"""{
     |  "name": "${jsonEscape(packageName)}",
     |  "description": "${jsonEscape(description)}",
     |  "version": "${jsonEscape(version)}",
     |  "main": "${jsonEscape(mainFile)}",
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
     |    "registry": "${jsonEscape(registryUrl)}"
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

  // Remplace IO.delete(npmDir) par :
  IO.delete(npmDir / s"$projectName.js")
  IO.delete(npmDir / s"$projectName.js.map")
  IO.delete(npmDir / s"$projectName-fastopt.js.map")
  IO.delete(npmDir / "package.json")
  IO.createDirectory(npmDir)
  IO.copyFile(bundledJs, outputJs)

  val includedFiles0 = Seq(s"$projectName.js")
  val includedFiles =
    if (!optimized && sourceMap.exists()) {
      IO.copyFile(sourceMap, npmDir / s"$projectName.js.map")
      IO.copyFile(sourceMap, npmDir / s"$projectName-fastopt.js.map")
      includedFiles0 :+ s"$projectName.js.map"
    } else includedFiles0

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

def patchSourceMapWithContents(
  mapFile: File,   // maintenant unravel-rdf.min.js.map
  npmDir: File,
  baseDir: File,
  cdnDir: File,
  log: Logger
): Unit = {
  if (!mapFile.exists())
    sys.error(s"Missing source map: ${mapFile.getAbsolutePath}")

  val scriptFile = IO.createTemporaryDirectory / "patch_sourcemap.py"
  val script =
    "import json, os\n" +
      "with open('" + mapFile.getAbsolutePath + "') as f:\n" +
      "    m = json.load(f)\n" +
      "sources = m.get('sources', [])\n" +
      "contents = []\n" +
      "rewritten_sources = []\n" +
      "for s in sources:\n" +
      "    stripped = s\n" +
      "    if stripped.startswith('webpack:///'):\n" +
      "        stripped = stripped[len('webpack:///'):]\n" +
      "    elif stripped.startswith('webpack://'):\n" +
      "        stripped = stripped[len('webpack://'):]\n" +
      "    while stripped.startswith('./'):\n" +
      "        stripped = stripped[2:]\n" +
      "    while stripped.startswith('../'):\n" +
      "        stripped = stripped[3:]\n" +
      "    if os.path.isabs(stripped):\n" +
      "        resolved = os.path.normpath(stripped)\n" +
      "    else:\n" +
      "        resolved = os.path.normpath(os.path.join('" + baseDir.getAbsolutePath + "', stripped))\n" +
      "    if os.path.exists(resolved):\n" +
      "        with open(resolved, encoding='utf-8', errors='replace') as sf:\n" +
      "            contents.append(sf.read())\n" +
      "        rewritten_sources.append('file://' + resolved)\n" +
      "    else:\n" +
      "        contents.append(None)\n" +
      "        rewritten_sources.append(s)\n" +
      "m['sources'] = rewritten_sources\n" +
      "m['sourcesContent'] = contents\n" +
      "resolved_count = sum(1 for c in contents if c is not None)\n" +
      "total = len(contents)\n" +
      "with open('" + mapFile.getAbsolutePath + "', 'w') as f:\n" +
      "    json.dump(m, f)\n" +
      "print('Patched: ' + str(resolved_count) + ' / ' + str(total) + ' sources resolved')\n"

  IO.write(scriptFile, script)
  val result = Process(Seq("python3", scriptFile.getAbsolutePath)).!(log)
  if (result != 0) sys.error("Failed to patch source map with sourcesContent")
  log.info(s"Patched ${mapFile.getName} in place")
}

def runCdnBundle(
  npmDir: File,
  cdnDir: File,
  baseDir: File,
  webpackConfig: String,
  log: Logger,
  taskLabel: String,
  debug: Boolean = false
): File = {
  val nodeModules = npmDir / "node_modules"
  val packageJson = npmDir / "package.json"

  // npm install seulement si node_modules absent ou package.json plus récent
  val needsInstall = !nodeModules.exists() ||
    packageJson.lastModified > nodeModules.lastModified

  if (needsInstall) {
    log.info(s"Installing npm dependencies in ${npmDir.getAbsolutePath}...")
    val installResult = Process("npm install", npmDir).!(log)
    if (installResult != 0) sys.error(s"npm install failed in ${npmDir.getAbsolutePath}")
  } else {
    log.info("npm dependencies up to date, skipping install.")
  }

  log.info(s"Running webpack $taskLabel bundle...")
  val env = Seq(
    "UNRAVEL_ENTRY"       -> (npmDir / "unravel-rdf.js").getAbsolutePath,
    "UNRAVEL_OUTPUT_PATH" -> cdnDir.getAbsolutePath,
    "UNRAVEL_DEBUG"       -> (if (debug) "1" else "0")
  )
  val bundleResult = Process(
    Seq("npx", "webpack-cli", "--config", webpackConfig, "--no-cache"),
    npmDir,
    env: _*
  ).!(log)
  if (bundleResult != 0) sys.error(s"webpack $taskLabel bundle failed")

  // Patch le map APRES webpack pour ne pas se faire ecraser
  if (debug) {
    val webpackMap = cdnDir / "unravel-rdf.min.js.map"
    patchSourceMapWithContents(webpackMap, npmDir, baseDir, cdnDir, log)
  }

  log.info(s"$taskLabel bundle ready in ${cdnDir.getAbsolutePath}")
  cdnDir
}

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    organization := "fr.inrae.metabohub.p2m2",
    organizationName := "p2m2",
    name := "unravel-rdf",
    version := sys.env.getOrElse("UNRAVEL_RDF_VERSION", buildTime),
    scalaVersion := "2.13.18",
    organizationHomepage := Some(url("https://www6.inrae.fr/p2m2")),
    licenses := Seq(
      "GPL-3.0-or-later" -> url("https://www.gnu.org/licenses/gpl-3.0.html")
    ),
    homepage := Some(url("https://forge.inrae.fr/p2m2/unravel-rdf")),
    description := "Unravel RDF graphs — interactive SPARQL session management with lazy pagination, serialization, and graph traversal.",

    useYarn := false,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    Test / parallelExecution := false,
    webpackBundlingMode := BundlingMode.LibraryOnly(),

    scalacOptions ++= Seq(
      "-deprecation",
      "-feature"
    ),

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
      "qs" -> npmQsVersion,
      "webpack" -> npmWebpackVersion,
      "webpack-cli" -> npmWebpackCliVersion,
      "source-map-loader" -> npmSourceMapLoaderVersion
    ),

    Test / npmDependencies ++= (Compile / npmDependencies).value,

    Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withOptimizer(false).withPrettyPrint(true).withSourceMap(true) },
    Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false).withModuleKind(ModuleKind.CommonJSModule) },

    Compile / sourceGenerators += Def.task {
      val file =
        baseDirectory.value /
          "src" / "main" / "scala" /
          "fr" / "inrae" / "metabohub" /
          "semantic_web" /
          "UnravelSessionVersionAtBuildTime.scala"

      val bt = buildTime

      val content =
        s"""|package fr.inrae.metabohub.semantic_web
            |
            |object UnravelSessionVersionAtBuildTime {
            |  val version: String = "$buildTime"
            |}
            |""".stripMargin

      IO.write(file, content)
      Seq(file)
    }.taskValue,

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
      val npmDir        = npmPrepareRelease.value
      val cdnDir        = baseDirectory.value / "target" / "cdn"
      val webpackConfig = (baseDirectory.value / "webpack.cdn.config.js").getAbsolutePath
      val log           = streams.value.log
      runCdnBundle(npmDir, cdnDir, baseDirectory.value, webpackConfig, log, taskLabel = "CDN", debug = false)
    },

    cdnDebugPrepare := {
      val npmDir        = npmPrepareDebugRelease.value
      val cdnDir        = baseDirectory.value / "target" / "cdn-debug"
      val webpackConfig = (baseDirectory.value / "webpack.cdn.config.js").getAbsolutePath
      val log           = streams.value.log
      runCdnBundle(npmDir, cdnDir, baseDirectory.value, webpackConfig, log, taskLabel = "CDN debug", debug = true)
    }
  )

Global / onChangedBuildSource := ReloadOnSourceChanges