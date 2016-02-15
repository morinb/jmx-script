// Project name (artifact name in Maven)
name := "jmx-script"

// organization name (e.g., the package name of the project)
organization := "com.bnpparibas.grp.jmx"

version := "0.1.4"

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

// Default main class to run : sbt run
// the jar can be directly run with 'java -jar' command.
mainClass in (Compile, run) := Some("JmxScript")


pomExtra := <build>
  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.1</version>
      </plugin>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>build-helper-maven-plugin</artifactId>
        <version>1.9</version>
      </plugin>
    </plugins>
  </pluginManagement>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <executions>
        <execution>
          <phase>compile</phase>
          <goals>
            <goal>compile</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <source>1.6</source>
        <target>1.6</target>
      </configuration>
    </plugin>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>build-helper-maven-plugin</artifactId>
      <executions>
        <execution>
          <id>add-source</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>add-source</goal>
          </goals>
          <configuration>
            <sources>
              <source>src/main/scala</source>
            </sources>
          </configuration>
        </execution>
        <execution>
          <id>add-test-source</id>
          <phase>generate-sources</phase>
          <goals>
            <goal>add-test-source</goal>
          </goals>
          <configuration>
            <sources>
              <source>src/test/scala</source>
            </sources>
          </configuration>
        </execution>
      </executions>
    </plugin>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>2.3</version>
      <executions>
        <!-- Run shade goal on package phase -->
        <execution>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
          <configuration>
            <transformers>
              <!-- add Main-Class to manifest file -->
              <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                <mainClass>com.bnpparibas.grp.jmx.script.JmxScript</mainClass>
              </transformer>
            </transformers>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>

// Dependencies
val weblogic_full_client = "bea" % "wlfullclient" % "10.3.2"
val commons_lang = "commons-lang" % "commons-lang" % "2.6"
val commons_cli = "commons-cli" % "commons-cli" % "1.3.1"

// library dependencies. (organization name) % (project name) % (version)
libraryDependencies ++= Seq(
  weblogic_full_client,
  commons_lang,
  commons_cli
)