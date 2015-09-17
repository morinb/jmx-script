// Project name (artifact name in Maven)
name := "jmx-script"

// organization name (e.g., the package name of the project)
organization := "com.bnpparibas.grp.jmx"

version := "0.1.0"

// project description
description := "JMX Java Console library for script use."

// Enables publishing to maven repo
publishMavenStyle := true

/* execute 'sbt publish' to put jar in this repo: */
publishTo := Some(Resolver.file("file", new File("C:\\workspace\\maven-3.0.4\\repository")))

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

// library dependencies. (organization name) % (project name) % (version)
libraryDependencies ++= Seq()