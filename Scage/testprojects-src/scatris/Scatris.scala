package scatris

import figures._
import su.msk.dunno.scage.support.tracer.{Trace, State, StandardTracer}
import su.msk.dunno.scage.handlers.{Renderer, AI}
import su.msk.dunno.scage.support.messages.Message
import su.msk.dunno.scage.support.{ScageProperties, ScageLibrary}
import org.lwjgl.input.Keyboard
import su.msk.dunno.scage.handlers.controller.Controller

object Scatris extends Application with ScageLibrary {
  properties = "scatris-properties.txt"

  private def isRestingPoint(point:List[Trace[State]]) = point.find(trace => {
    val state = trace.getState
    state.contains("isActive") && state.getBool("isActive") &&
    state.contains("isMoving") && !state.getBool("isMoving")
  }) match {
    case Some(trace) => true
    case None => false
  }

  def isFullRow(y:Int) = {
    val matrix = StandardTracer.matrix
    (0 to StandardTracer.N_x-1).foldLeft(true)((is_full, x) => is_full && isRestingPoint(matrix(x)(y)))
  }

  def disableRow(y:Int) = {
    val matrix = StandardTracer.matrix
    (0 to StandardTracer.N_x-1).foreach(x => matrix(x)(y).foreach(trace => trace.changeState(new State("disable"))))
  }

  def getRandomFigure = {
    val rand = math.random
    if(rand < 0.14) new G_Figure(upperCenter)
    else if(rand >= 0.14 && rand < 0.28) new G_Inverted_Figure(upperCenter)
    else if(rand >= 0.28 && rand < 0.42) new Line(upperCenter)
    else if(rand >= 0.42 && rand < 0.56) new S_Figure(upperCenter)
    else if(rand >= 0.56 && rand < 0.70) new S_Inverted_Figure(upperCenter)
    else if(rand >= 0.70 && rand < 0.84) new Square(upperCenter)
    else new T_Figure(upperCenter)
  }

  var score = 0
  private var is_game_finished = false
  def upperCenter = StandardTracer.pointCenter(N_x/2, N_y-2)
  var figure = getRandomFigure
  AI.registerAI(() => {
    for(y <- 0 to N_y-1) {
      if(isFullRow(y)) {
        disableRow(y)
        score += N_x
      }
    }

    if(!figure.canMoveDown && !is_game_finished) {
      figure = getRandomFigure
      if(!figure.canMoveDown) is_game_finished = true
    }
  })
  
  Renderer.addInterfaceElement(() => {
    Message.print("score: "+score, 20, height-25)
    if(is_game_finished) Message.print("Game Over", 20, height-45)
  })

  // game pause
  Controller.addKeyListener(Keyboard.KEY_SPACE, () => switchPause)
  Renderer.addInterfaceElement(() => if(on_pause)Message.print("PAUSE", width/2-20, height/2+60))

  start
}