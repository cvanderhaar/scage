package su.msk.dunno.tutorials.tetris

import net.scage.support.tracer3._
import net.scage.ScageScreenApp
import net.scage.support.{ScageColor, State, Vec}
import net.scage.ScageLib._

object Tetris extends ScageScreenApp("Tetris") {
  val tracer = new TetrisTracer

  // TODO: throw error if nothing match
  def randomFigure:Figure = {
    val num = (math.random*7).toInt
    num match {
      case 0 => new G_Figure
      case 1 => new G_Inverted_Figure
      case 2 => new Line
      case 3 => new S_Figure
      case 4 => new S_Inverted_Figure
      case 5 => new Square
      case 6 => new T_Figure
      case _ => new Square
    }
  }

  private var next_figure = randomFigure
  private var current_figure = randomFigure
  current_figure.moveInDir(Vec(-6, 0))
  
  key(KEY_L, onKeyDown = {
    tracer.removeTraces(next_figure.parts:_*)
    next_figure = new Line
  })

  val leftright_speed = property("speed.leftright", 70)
  key(KEY_RIGHT, leftright_speed, onKeyDown = {
    current_figure.moveIfPossible(Vec(1,0))
  });
  key(KEY_LEFT, leftright_speed, onKeyDown = {
    current_figure.moveIfPossible(Vec(-1,0))
  });

  private var current_orientation = 0
  key(KEY_UP, onKeyDown = {
    if(!onPause && current_figure.orientations.keys.size > 0 && current_figure.orientations(current_orientation)()) {
      current_orientation += 1
      if(current_orientation > current_figure.orientations.keys.size - 1) current_orientation = 0
    }
  })

  val down_speed = property("speed.down", 30)
  key(KEY_DOWN, down_speed, onKeyDown = {
    current_figure.moveIfPossible(Vec(0,-1))
  })

  key(KEY_SPACE, onKeyDown = switchPause())
  interface {
    if(onPause) print("PAUSE", window_width/2, window_height/2, YELLOW)
  }

  action(tracer.movingdownSpeed) {
    if(!current_figure.moveIfPossible(Vec(0,-1))) {
      current_figure.land()
      current_figure = next_figure
      current_figure.moveInDir(Vec(-6, 0))
      next_figure = randomFigure
      if(!current_figure.isDirPossible(Vec(0,0))) {
        for(i <- tracer.N_y-1 to 0 by -1) tracer.disableRow(i)
        next_figure = randomFigure
      }
      current_orientation = 0
    }
  }

  run()
}

import Tetris._

class TetrisTracer extends ScageTracer[FigurePart](solid_edges = true) {
  private var count = 0
  interface {
    print(count, 20, window_height-20, YELLOW)
    /*print(current_figure.name, 20, height-40, YELLOW)*/
  }
  def movingdownSpeed:Long = {
    if(     count < 10) 200
    else if(count < 20) 175
    else if(count < 30) 150
    else if(count < 40) 125
    else                100
  }

  //backgroundColor = WHITE
  render {
    for {
      x <- 0 until N_x
      y <- 0 until N_y
      point = point_matrix(x)(y)
      if point.size > 0
      figure_part = point.head
    } {
      drawFilledRectCentered(pointCenter(figure_part.location), h_x-1, h_y-1, figure_part.color)
    }
  }

  def isFullRow(y:Int) = {
    y >= 0 && y < N_y && (0 until N_x).foldLeft(true)((is_full, x) => is_full && point_matrix(x)(y).size > 0)
  }

  def disableRow(y:Int) {
    if(y >= 0 && y < N_y) {
      (0 until N_x).foreach(x => point_matrix(x)(y).foreach(trace => removeTraces(trace)))
      for {
        y <- y+1 until N_y
        x <- 0 until N_x
        point = point_matrix(x)(y)
        if point.size > 0
        figure_part = point.head
      } moveTrace(figure_part, Vec(0,-1))
    }
  }

  action(movingdownSpeed) {
    for(y <- 0 until N_y) if(isFullRow(y)) {
      disableRow(y)
      count += 1
    }
  }
}

class FigurePart(val color:ScageColor) extends Trace {
  protected val _state = State("color" -> color)
  def state = _state

  type ChangerType = FigurePart
  def changeState(changer:FigurePart, s:State) {
    if(s.contains("land")) _state += ("landed" -> true)
  }
}

import Tetris.tracer._

trait Figure {
  def name:String

  val figure_colors = RED :: GREEN :: YELLOW :: BLUE :: ORANGE :: Nil
  protected def randomFigureColor = figure_colors((math.random*figure_colors.size).toInt)
  val color = randomFigureColor

  val init_coord = Vec(N_x/2+6, N_y-1)
  def parts:List[FigurePart]

  def isPositionPossible(position:Vec) = {
    isPointOnArea(position) && !tracesInPoint(position).exists(part_in_position => {
      part_in_position.state.contains("landed")
    })
  }

  def isDirPossible(dir:Vec) = {
    parts.exists(part => containsTrace(part.id)) &&
    parts.forall(part => !containsTrace(part.id) || isPositionPossible(part.location + dir))
  }

  def moveInDir(dir:Vec) {
    parts.foreach(part => if(containsTrace(part.id)) moveTrace(part, dir))
  }

  def moveIfPossible(dir:Vec) = {
    val is_dir_possible = !onPause && isDirPossible(dir)
    if(is_dir_possible) moveInDir(dir)
    is_dir_possible
  }

  def land() {
    parts.foreach(part => part.changeState(null, State("land" -> true)))
  }

  def addPart(point:Vec) = {
    addTrace(point, new FigurePart(color))
  }

  def orientations:Map[Int, () => Boolean]
  protected def generateOrientations(positions_array:List[Vec]*):Map[Int, () => Boolean] = {
    def addOrientation(orientations:Map[Int, () => Boolean], next:Int):Map[Int, () => Boolean] = {
      if(next >= positions_array.length) orientations
      else {
        val new_orientations = orientations + (next -> (() => {
          val canMove = (0 until parts.length).foldLeft(true)((can_move, part_number) => {
            val part = parts(part_number)
            val position = part.location + positions_array(next)(part_number)
            can_move && isPositionPossible(position)
          })
          if(canMove) {
            (0 until parts.length).foreach(part_number => {
              val part = parts(part_number)
              val step = positions_array(next)(part_number)
              moveTrace(part, step)
            })
            true
          }
          else false
        }))
        addOrientation(new_orientations, next+1)
      }
    }
    addOrientation(Map(), 0)
  }
}

class G_Figure extends Figure {
  val name = "G_Figure"

  val parts = addPart(init_coord + Vec(-2,  0)) ::
              addPart(init_coord + Vec(-2, -1)) ::
              addPart(init_coord + Vec(-1, -1)) ::
              addPart(init_coord + Vec( 0, -1)) :: Nil

  val orientations = generateOrientations(
    List(Vec( 2,  0), Vec( 1,  1), Vec(0, 0), Vec(-1, -1)),
    List(Vec( 0, -2), Vec( 1, -1), Vec(0, 0), Vec(-1,  1)),
    List(Vec(-2,  0), Vec(-1, -1), Vec(0, 0), Vec( 1,  1)),
    List(Vec( 0,  2), Vec(-1,  1), Vec(0, 0), Vec( 1, -1))
  )
}

class G_Inverted_Figure extends Figure {
  val name = "G_Inverted_Figure"

  val parts = addPart(init_coord + Vec(-2, -1)) ::
              addPart(init_coord + Vec(-1, -1)) ::
              addPart(init_coord + Vec( 0, -1)) ::
              addPart(init_coord)               :: Nil

  val orientations = generateOrientations(
    List(Vec( 1,  1), Vec(0, 0), Vec(-1, -1), Vec( 0, -2)),
    List(Vec( 1, -1), Vec(0, 0), Vec(-1,  1), Vec(-2,  0)),
    List(Vec(-1, -1), Vec(0, 0), Vec( 1,  1), Vec( 0,  2)),
    List(Vec(-1,  1), Vec(0, 0), Vec( 1, -1), Vec( 2,  0))
  )
}

class Line extends Figure {
  val name = "Line"

  val parts = addPart(init_coord + Vec(-2, 0)) ::
              addPart(init_coord + Vec(-1, 0)) ::
              addPart(init_coord)              ::
              addPart(init_coord + Vec( 1, 0)) :: Nil

  val orientations = generateOrientations(List(Vec( 2,  2), Vec( 1,  1), Vec(0, 0), Vec(-1, -1)),
                                                   List(Vec(-2, -2), Vec(-1, -1), Vec(0, 0), Vec( 1,  1))
  )
}

class S_Figure extends Figure {
  val name = "S_Figure"

  val parts = addPart(init_coord + Vec(-2, -1)) ::
              addPart(init_coord + Vec(-1, -1)) ::
              addPart(init_coord + Vec(-1, 0)) ::
              addPart(init_coord + Vec( 0, 0)) :: Nil

  val orientations = generateOrientations(
    List(Vec( 1, 1),  Vec(0, 0), Vec( 1, -1), Vec(0, -2)),
    List(Vec(-1, -1), Vec(0, 0), Vec(-1,  1), Vec(0,  2))
  )
}

class S_Inverted_Figure extends Figure {
  val name = "S_Inverted_Figure"

  val parts = addPart(init_coord + Vec(-2,  0)) ::
              addPart(init_coord + Vec(-1,  0)) ::
              addPart(init_coord + Vec(-1, -1)) ::
              addPart(init_coord + Vec( 0, -1)) :: Nil

  val orientations = generateOrientations(
    List(Vec(0, -2), Vec(-1, -1), Vec(0, 0), Vec(-1,  1)),
    List(Vec(0,  2), Vec( 1,  1), Vec(0, 0), Vec( 1, -1))
  )
}

class Square extends Figure {
  val name = "Square"

  val parts = addPart(init_coord + Vec(0, 0)) ::
              addPart(init_coord + Vec(1, 0)) ::
              addPart(init_coord + Vec(0, -1)) ::
              addPart(init_coord + Vec(1, -1)) :: Nil

  val orientations:Map[Int, () => Boolean] = Map()
}

class T_Figure extends Figure {
  val name = "T_Figure"

  val parts = addPart(init_coord + Vec(-2, -1)) ::
              addPart(init_coord + Vec(-1, -1)) ::
              addPart(init_coord + Vec( 0, -1)) ::
              addPart(init_coord + Vec(-1,  0)) :: Nil

  val orientations = generateOrientations(
    List(Vec(1,   1),  Vec(0, 0), Vec(-1, -1), Vec( 1, -1)),
    List(Vec(1,  -1),  Vec(0, 0), Vec(-1,  1), Vec(-1, -1)),
    List(Vec(-1, -1),  Vec(0, 0), Vec( 1,  1), Vec(-1,  1)),
    List(Vec(-1,  1),  Vec(0, 0), Vec( 1, -1), Vec( 1,  1))
  )
}
