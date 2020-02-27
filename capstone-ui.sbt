val capstoneUI =
  project.in(file("capstone-ui"))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      scalaVersion := "2.11.12",
      // Add the sources of the main project
      unmanagedSources in Compile ++= {
        val rootSourceDirectory = (scalaSource in (root, Compile)).value / "observatory"
        Seq(
          rootSourceDirectory / "Interaction2.scala",
          rootSourceDirectory / "Signal.scala",
          rootSourceDirectory / "models.scala",
          rootSourceDirectory / "package.scala"
        )
      },
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.4",
        "com.lihaoyi" %%% "scalatags" % "0.6.0"
      ),
      scalaJSUseMainModuleInitializer := true
    )