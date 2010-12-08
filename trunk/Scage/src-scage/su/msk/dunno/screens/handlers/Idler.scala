package su.msk.dunno.screens.handlers

import su.msk.dunno.scage.support.ScageProperties._
object Idler {
  val framerate = property("framerate", 100)
}

class Idler {
    var fps:Int = 0

    private var msek = System.currentTimeMillis
    private var frames:Int = 0
    private def countFPS() = {
      frames += 1
      if(System.currentTimeMillis - msek >= 1000) {
        fps = frames
        frames = 0
        msek = System.currentTimeMillis
      }
    }

    private val sleep:Long = if(Idler.framerate != 0) 1000/Idler.framerate else 10
    def idle = {
      countFPS
      Thread.sleep(sleep)
    }
  }
