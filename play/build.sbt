import EndpointsSettings._

val `algebra-jvm` = LocalProject("algebraJVM")
val `algebra-circe-jvm` = LocalProject("algebra-circeJVM")
val `testsuite-jvm` = LocalProject("testsuiteJVM")
val `json-schema-circe-jvm` = LocalProject("json-schema-circeJVM")
val `json-schema-playjson-jvm` = LocalProject("json-schema-playjsonJVM")

val `play-server` =
  project.in(file("server"))
    .settings(
      publishSettings,
      `scala 2.11 to latest`,
      name := "endpoints-play-server",
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play-netty-server" % playVersion,
        "com.typesafe.play" %% "play-test" % playVersion % Test
      )
    )
    .dependsOn(`algebra-jvm` % "test->test;compile->compile")
    .dependsOn(`algebra-circe-jvm` % "test->test")

val `play-server-circe` =
  project.in(file("server-circe"))
    .settings(
      publishSettings,
      `scala 2.11 to latest`,
      name := "endpoints-play-server-circe",
      libraryDependencies += "io.circe" %% "circe-parser" % circeVersion
    )
    .dependsOn(`play-server`, `algebra-circe-jvm`, `json-schema-circe-jvm`)

val `play-server-playjson` =
  project.in(file("server-playjson"))
    .settings(
      publishSettings,
      `scala 2.11 to latest`,
      name := "endpoints-play-server-playjson",
      libraryDependencies += "com.typesafe.play" %% "play-json" % playjsonVersion
    )
    .dependsOn(`play-server`, `json-schema-playjson-jvm`)

val `play-client` =
  project.in(file("client"))
    .settings(
      publishSettings,
      `scala 2.11 to latest`,
      name := "endpoints-play-client",
      libraryDependencies += "com.typesafe.play" %% "play-ahc-ws" % playVersion
    )
    .dependsOn(`algebra-jvm` % "test->test;compile->compile", `algebra-circe-jvm` % "compile->test;test->test")
