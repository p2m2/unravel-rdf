addSbtPlugin("org.jetbrains.scala"% "sbt-ide-settings"              % "1.1.2")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.19.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.3.2")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"                 % "2.3.1")
addSbtPlugin("com.github.sbt"     % "sbt-release"                   % "1.4.0")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"           % "0.21.1")
addSbtPlugin("io.crashbox"        % "sbt-gpg"                       % "0.2.1")
addSbtPlugin("com.eed3si9n"       % "sbt-assembly"                  % "2.3.1")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta44")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"
