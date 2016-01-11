IMPORTANT NOTE:
The project is moved to github:
http://github.com/dunnololda/scage
You can find the latest version with lots of api improvements there.

![http://scage.googlecode.com/svn/trunk/ScageMaven/scage-logo.png](http://scage.googlecode.com/svn/trunk/ScageMaven/scage-logo.png)

## Introduction. ##
Scage is a framework to write simple 2D opengl games. It is written in Scala (http://scala-lang.org/) and based on several java libraries:
  * phys2d as a physics engine (http://phys2d.cokeandcode.com/)
  * lwjgl as an opengl wrapper (http://lwjgl.org)
  * slick as a resource and texture loader (http://slick.cokeandcode.com/)

The main purpose of this project is to give a convenient tool for game-developers to write a code of pure functionality without any boilerplate.

## Features. ##
  * Architechture similar to actors framework with different kinds of tasks executing on different stages of app lifecycle. Simililar to actors these tasks are anonymous functions, and you can add and remove them in runtime in any scope of your app. Its all singlethreaded, so you dont have to mess with messages.
  * Vast drawing library for any kinds of 2D opengl primitives.
  * Loading and utilizing fonts from ttf-files (based on 'Slick2D' api but with improvements).
  * i18n: loading strings and even the whole interfaces from xml files. Runtime language change.
  * Framework to build in-game interfaces from xml files of simple structure.
  * App settings can be specified in a text files as a key-value pairs. Lots of engine options are set that way (alongside with the standard possibility to set them as parameters) allowing fine-tuning without app rebuilding.
  * Tracers framework: easy game objects tracking and interacting on a two-dimensional game map.
  * Lightweight wrapper upon phys2d engine.
  * Easy app building/deploing (as a standalone or via webstart) using maven infrastructure.
  * Multiple platforms support: Windows, Linux, Mac, Solaris (thanks to Java and lwjgl actually). Similar build process for any platform (with maven).
  * Client/server network api upon actors with simple text protocol based on json format.

## Hello World Example. ##

Rotating 'Hello World!' label
```
import net.scage.ScageScreenApp
import net.scage.ScageLib._
import net.scage.support.Vec

object HelloWorldExample extends ScageScreenApp("Hello World") {
  private var ang = 0f
  actionStaticPeriod(100) {
    ang += 5
  }

  backgroundColor = BLACK
  render {
    openglMove(windowSize/2)
    openglRotate(ang)
    print("Hello World!", Vec(-50, -5), GREEN)
  }
}
```
![http://scage.googlecode.com/svn/trunk/ScageMaven/rotating_hello.png](http://scage.googlecode.com/svn/trunk/ScageMaven/rotating_hello.png)
Network api example: client sends to server random 2d vectors and server sends back corresponded normalized values
```
import net.scage.ScageApp
import net.scage.support.net.{NetClient, NetServer}
import net.scage.support.{Vec, State}

object EchoExample extends ScageApp("Echo") {
  NetServer.startServer(
    port = 9800,
    onNewConnection = {
      client => client.send(State("hello" -> "send me vec and I send you back its n!"))
      (true, "")
    },
    onClientDataReceived = {
      (client, received_data) => received_data.neededKeys {
        case ("vec", vec:Vec) => client.send(State(("n" -> vec.n)))
      }
    }
  )

  NetClient.startClient(
    server_url = "localhost",
    port = 9800,
    onServerDataReceived = {
      received_data => received_data.neededKeys {
        case ("hello", hello_msg) =>
          val random_vec = Vec((math.random*100).toInt, (math.random*100).toInt)
          println("sending vec: "+random_vec)
          NetClient.send(State(("vec" -> random_vec)))
        case ("n", n:Vec) =>
          println("received n: "+n)
          println("waiting 5 sec...")
          Thread.sleep(5000)
          val random_vec = Vec((math.random*100).toInt, (math.random*100).toInt)
          println("sending vec: "+random_vec)
          NetClient.send(State("vec" -> random_vec))
      }
    }
  )

  dispose {
    NetServer.stopServer()
    NetClient.stopClient()
  }
}
```


## More examples. ##

### Tetris. ###
Simple tetris clone. Controls: arrow keys, spacetab - pause.

Source code: http://code.google.com/p/scage/source/browse/trunk/tetris/src/main/scala/su/msk/dunno/tutorials/tetris/Tetris.scala

Launch webstart: http://fzeulf.netris.ru/bor/tetris/run.jnlp

![http://scage.googlecode.com/svn/trunk/tetris/tetris.png](http://scage.googlecode.com/svn/trunk/tetris/tetris.png)

### Snake. ###
Snake clone. Controls: arrow keys

Source code: http://code.google.com/p/scage/source/browse/trunk/snake/src/main/scala/su/msk/dunno/scage/tutorials/snake/Snake.scala

Launch webstart: http://fzeulf.netris.ru/bor/snake/run.jnlp

![http://scage.googlecode.com/svn/trunk/snake/snake.png](http://scage.googlecode.com/svn/trunk/snake/snake.png)

### Arcanoid. ###
Physics demo. Bounce ball to blocks to make them disappear and bring you points. Controls: left and right arrow keys

Source code: http://code.google.com/p/scage/source/browse/#svn%2Ftrunk%2FScarcanoid%2Fsrc%2Fmain%2Fscala%2Fsu%2Fmsk%2Fdunno%2Fscar

Launch webstart: http://fzeulf.netris.ru/bor/scar/run.jnlp

![http://scage.googlecode.com/svn/trunk/Scarcanoid/scaranoid.png](http://scage.googlecode.com/svn/trunk/Scarcanoid/scaranoid.png)

### Life. ###
"Tracers"-framework example. Conway's Game of Life, the model of "glider gun". No controls (since its not a real game) - just press spacetab to start the process.

Source code: http://code.google.com/p/scage/source/browse/trunk/life/src/main/scala/su/msk/dunno/scage/tutorials/life/Life.scala

Launch webstart: http://fzeulf.netris.ru/bor/life/run.jnlp

![http://scage.googlecode.com/svn/trunk/life/life.png](http://scage.googlecode.com/svn/trunk/life/life.png)

### Uke ###
Physics and texture handling demo. Run and jump avoiding abysses and obstacles. Controls: Z to jump, X to destroy obstacle, down arrow key to fast land after high jump.

Source code: http://code.google.com/p/scage/source/browse/trunk/uke/src/main/scala/su/msk/dunno/scage/uke/Uke.scala

Launch webstart: http://fzeulf.netris.ru/bor/uke/run.jnlp

![http://scage.googlecode.com/svn/trunk/uke/uke.png](http://scage.googlecode.com/svn/trunk/uke/uke.png)

### Runnegun ###
Some kind of top-down shooter. Destory circles with bullets and avoid their bullets. Controls: WASD to move, left mouse button to shoot.

Source code: http://code.google.com/p/scage/source/browse/#svn%2Ftrunk%2Frunnegun%2Fsrc%2Fmain%2Fscala%2Fsu%2Fmsk%2Fdunno%2Frunnegun

Launch webstart: http://fzeulf.netris.ru/bor/runnegun/run.jnlp

![http://scage.googlecode.com/svn/trunk/runnegun/runnegun.png](http://scage.googlecode.com/svn/trunk/runnegun/runnegun.png)

### Jet Flight ###
Game with textures example. Control a plane against very simple AI bot. Fly with left and right arrows, speed-up with up-arrow, shoot with left control.

Source code: https://github.com/dunnololda/scage-projects/blob/master/jetflight/src/main/scala/net/scage/tutorial/jetflight/JetFlight.scala

Launch webstart: http://fzeulf.netris.ru/bor/jetflight/run.jnlp

![https://github.com/dunnololda/scage-projects/raw/master/jetflight/jetflight.png](https://github.com/dunnololda/scage-projects/raw/master/jetflight/jetflight.png)

### Blases ###
Shoot bubbles to reach the finish point. The game demonstrates i18n, resolution changing, one way to create menus and some other things.

Source code: https://github.com/dunnololda/scage-projects/tree/master/blases

Launch webstart: http://fzeulf.netris.ru/bor/blases/run.jnlp

![https://github.com/dunnololda/scage-projects/raw/master/blases/blases.png](https://github.com/dunnololda/scage-projects/raw/master/blases/blases.png)

### Pong ###
A very simple clone of Classic Pong (of its realization in Nicol Examples actually, https://github.com/philcali/Nicol-examples/tree/master/pong). Control paddles with mouse. The ball speed is slightly increased as you bounce it from the paddles.

Source code: https://github.com/dunnololda/scage-projects/blob/master/pong/src/main/scala/net/scage/projects/pong/Pong.scala

Launch webstart: http://fzeulf.netris.ru/bor/pong/run.jnlp

![https://github.com/dunnololda/scage-projects/raw/master/pong/pong.png](https://github.com/dunnololda/scage-projects/raw/master/pong/pong.png)

## Installation. ##

### For maven users. ###
Add to your pom.xml:
```
<repositories>
...
    <repository>
      <id>scage</id>
      <name>Scage Maven Repo</name>
      <url>http://scage.googlecode.com/svn/maven-repository</url>
    </repository>
</repositories>
...
<dependencies>
...
    <dependency>
        <groupId>su.msk.dunno</groupId>
        <artifactId>scage</artifactId>
        <version>0.8</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

You can use archetype to create new scage project stub:
```
$ mvn archetype:generate -DgroupId=my.company -DartifactId=app -Dversion=0.1 -Dpackage=my.company.app -DarchetypeGroupId=scage -DarchetypeArtifactId=project-archetype -DarchetypeVersion=0.8 -DarchetypeRepository=http://scage.googlecode.com/svn/maven-repository
```
To launch app you can type:<br />
$ mvn clean test

This project stub has two profiles in its pom.xml for app building. To build a standalone app type in your console:<br />
$ mvn clean package -Pbuild

Folder with executables will be created in "target" folder. Also there is a file "build.properties" where you can tune important options, such as target operating system (windows, linux, macosx, solaris), project main class, log level, etc.

To build a webstart app type:<br />
$ mvn clean package -Pwebstart

This command will create "jnlp" folder in "target". Then you can upload this folder to your host.

You also can use some IDE with good Maven and Scala support (for example, IntelliJ IDEA, http://www.jetbrains.com/idea/).

### For non-maven users. ###
Install maven =)

Also you can download jar-file, sources and example project of the latest version from "Downloads" page.

## Feedback. ##
Feel free to ask any questions by email or using issue tracker. Please excuse my English, I'm not a native English speaker, but I will try hard to answer your questions)