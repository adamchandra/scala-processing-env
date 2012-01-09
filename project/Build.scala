import sbt._
// import spde._
import Keys._
import Scope.{GlobalScope, ThisScope}

// import PluginKeys._


object BuildSettings {
  val buildOrganization = "scala-processing-dev"
  val buildScalaVersion = "2.9.1"
  val buildVersion = "0.1-SNAPSHOT"

  val buildSettings = Defaults.defaultSettings ++
  Seq (
    organization := buildOrganization,
    scalaVersion := buildScalaVersion,
    version := buildVersion,
    parallelExecution := true,
    fork := true,
    retrieveManaged := true,
    autoCompilerPlugins := true,
    // resolvers += ScalaToolsSnapshots,
    externalResolvers <<= resolvers map { rs =>
      Resolver.withDefaultResolvers(rs)},
    // unmanagedJars in Compile <<= baseDirectory map { base => (base / "lib" ** "*.jar").classpath },
    unmanagedBase <<= baseDirectory { base => base / ".." / "lib" },    
    moduleConfigurations ++= Resolvers.moduleConfigurations,
    javacOptions ++= Seq("-Xlint:unchecked"),
    // publishTo := Some(Resolvers.IESLSnapshotRepo),
    publishArtifact in (Compile, packageDoc) := false,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xcheckinit", "-encoding", "utf8"),
    shellPrompt := ShellPrompt.buildShellPrompt)
}

object ShellPrompt {

  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }

  val current = """\*\s+([^\s]+)""".r

  def gitBranches = ("git branch --no-color" lines_! devnull mkString)
  def hgBranch = ("hg branch" lines_! devnull mkString)

  val buildShellPrompt = {
    (state: State) => {
      val currBranch = hgBranch
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (currBranch, currProject, BuildSettings.buildVersion)
    }
  }
}



object Resolvers {
  val JavaNetRepo             = "java.net Repo" at "http://download.java.net/maven/2"
  val JlineRepo               = "JLine Repo" at "http://jline.sourceforge.net/m2repo"
  val TypesafeRepository      = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

  val LocalIvy                = Resolver.file("Local .ivy", Path.userHome / ".ivy2" / "local" asFile)
  val LocalM2                 = "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

  val DatabinderRepo = "Databinder Repository" at "http://databinder.net/repo"


  val moduleConfigurations = Seq(
    // ModuleConfiguration("edu.umass.cs.iesl", LocalM2)
  )
}

object Dependencies {
  val slf4jVersion = "1.6.1"
  val specs2Version = "1.4"
  val dispatchVersion = "0.7.8"

  val scalaTestVersion = "1.6.1"
  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  val scalaCheck = "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test"

  val specsVersion = "1.6.9"
  val specs = "org.scala-tools.testing" %% "specs" % specsVersion

  val specs2 = "org.specs2" %% "specs2" % specs2Version
  val slf4j = "org.slf4j" % "slf4j-api" % slf4jVersion
  val logbackClassic = "ch.qos.logback"     %   "logback-classic"     % "0.9.24"
  val logbackCore = "ch.qos.logback"     %   "logback-core"        % "0.9.24"

  val scalazCore    = "org.scalaz"         %% "scalaz-core"          % "6.0.3"
  val scalaj        = "org.scalaj"         %% "scalaj-collection"    % "1.2"

  val junit4        = "junit"              %  "junit"                % "4.4"


  // val processingVersion = propertyOptional[String]("1.1")
  // val spdeLib = spde_artifact
  // def spde_artifact = "us.technically.spde" %% "spde-core" % "0.3.1"
}


object SPDE extends Build {

  val buildShellPrompt = ShellPrompt.buildShellPrompt

  import Resolvers._
  import Dependencies._
  import BuildSettings._

  val commonDeps:Seq[sbt.ModuleID] =
    Seq(
      slf4j,
      logbackClassic,
      logbackCore,
      scalaTest,
      junit4,
      scalaCheck,
      scalazCore,
      scalaj
    )

  val printClasspath = TaskKey[File]("print-class-path")

  def printCp = (target, fullClasspath in Compile, compile in Compile) map { (out, cp, analysis) =>
    println(cp.files.map(_.getName).mkString("\n"))
    println("----")
    println(analysis.relations.allBinaryDeps.toSeq.mkString("\n"))
    println("----")
    println(out)
    out
  }

  lazy val spde:Project = Project(
    id = "spde",
    base = file("."),
    settings = buildSettings ++ Seq (libraryDependencies := commonDeps) 
  )

  lazy val straight_scala = Project(
    id = "Straight_Scala", 
    base = file("Straight_Scala"),
    settings = buildSettings ++ Seq (libraryDependencies := commonDeps) 
  )

  // lazy val explode = project("Explode", "Explode", new DefaultSpdeProject(_))
  // lazy val flocking = project("Flocking", "Flocking", new DefaultSpdeProject(_))
  // lazy val fold = project("Fold", "Fold", new DefaultSpdeProject(_))
  // lazy val continue = project("Continue", "Continue", new DefaultSpdeProject(_) with AutoCompilerPlugins {
  //   val continuations = compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.9.1")
  //   override def compileOptions = CompileOption("-P:continuations:enable") :: super.compileOptions.toList
  // })
  // lazy val list = project("List", "List", new DefaultSpdeProject(_))
  // lazy val gasket = project("Sierpinski", "Sierpinski_Gasket", new DefaultSpdeProject(_))
  // lazy val lsystems = project("L-Systems", "L_Systems", new DefaultSpdeProject(_))
  // lazy val matrix = project("Matrix", "Matrix", new DefaultOpenGLProject(_))
  // lazy val planerotate = project("PlaneRotate", "PlaneRotate", new DefaultSpdeProject(_))
  // lazy val ti_81 = project("TI-81", "TI-81", new DefaultSpdeProject(_))
  // lazy val geometry = project("Geometry", "Geometry", new DefaultOpenGLProject(_))
  // lazy val esfera = project("Esfera", "Esfera", new DefaultOpenGLProject(_))
  // lazy val fractalParticles = project("FractalParticles", "FractalParticles", new DefaultOpenGLProject(_))
  // lazy val spore1 = project("Spore1", "Spore1", new DefaultSpdeProject(_))
  // 
  // /* Video projects use GSVideo, see VIDEO.md for more info. */
  // lazy val loop = project("Loop", "Loop", new SampleVideoProject(_))
  // lazy val scratchP = project("Scratch", "Scratch", new SampleVideoProject(_))
  // 
  // lazy val trending = project("Trending", "Trending", new DefaultSpdeProject(_) {
  //   val dispatch = "net.databinder" %% "dispatch-lift-json" % "0.7.8"
  // })
  // lazy val android = project("Android", "Android", new AndroidProject(_) with SpdeAndroidProject {
  //   def androidPlatformName="android-2.0"
  //   override def sketchClass = "Fold"
  // })
  // lazy val straight_java = project("Straight_Java", "Straight_Java", new DefaultSpdeProject(_) {
  //   override def sketchClass = "StraightJava"
  //   // set the path below to your rt.jar / classes.jar, if you want applet export to work.
  //   // We can't find it automatically for plain Java projects.
  //   override def proguardOptions =
  //     "-libraryjars \"/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/classes.jar\"" :: super.proguardOptions
  // })


}
