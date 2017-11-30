lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    organization := "com.futurice",
    name := "minimal-play2",
    version := "1.0.3",
    scalaVersion := "2.12.3",
    libraryDependencies ++= Seq(
      guice,
      "org.sangria-graphql" %% "sangria" % "1.3.2",
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "io.circe" %% "circe-core" % "0.8.0",
      "io.circe" %% "circe-generic" % "0.8.0",
      "io.circe" %% "circe-parser" % "0.8.0",
      "io.circe" %% "circe-optics" % "0.8.0"
    ),
  )
