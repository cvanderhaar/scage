package su.msk.dunno.scage.test;

import su.msk.dunno.scage.single.support.ScageColors._
import su.msk.dunno.scage.screens.handlers.Renderer._
import org.lwjgl.input.Keyboard._
import su.msk.dunno.scage.single.support.messages.ScageMessage._
import su.msk.dunno.scage.screens.ScageScreen
import su.msk.dunno.scage.single.support.Vec
import su.msk.dunno.scage.screens.support.net.NetServer._
import su.msk.dunno.scage.screens.support.net.ClientHandler

import junit.framework._
import Assert._
import su.msk.dunno.scage.screens.support.tracer.{Trace, State, CoordTrace, CoordTracer}
import su.msk.dunno.scage.screens.support.physics.ScagePhysics
import su.msk.dunno.scage.screens.physics.support.objects.DynaBall

object ScageTest {
    def suite: Test = {
        val suite = new TestSuite(classOf[ScageTest]);
        suite
    }

    def main(args : Array[String]) {
        junit.textui.TestRunner.run(suite);
    }
}

/**
 * Unit test for simple App.
 */
class ScageTest extends TestCase("app") {

    /**
     * Rigourous Tests :-)
     */
    def testOK() {
      new ScageScreen("Hello World", is_main_screen = true, properties = "scagetest-properties.txt") {
        val tracer = new CoordTracer[CoordTrace]

        val trace = tracer.addTrace(Vec(width/2, height/2), new CoordTrace() {
          def getState = new State
          def changeState(changer:Trace, state:State) {}
        })

        val another_trace = tracer.addTrace(Vec(width/4, height/2), new CoordTrace() {
          def getState = new State
          def changeState(changer:Trace, state:State) {}
        })

        def moveIfFreeLocation(trace:CoordTrace, delta:Vec) {
          val new_location = trace.coord + delta
          if(!tracer.hasCollisions(trace, new_location, 20))
            tracer.updateLocation(trace, new_location)
        }

        key(KEY_UP,    10, onKeyDown = moveIfFreeLocation(trace, Vec(0,1)))
        key(KEY_DOWN,  10, onKeyDown = moveIfFreeLocation(trace, Vec(0,-1)))
        key(KEY_RIGHT, 10, onKeyDown = moveIfFreeLocation(trace, Vec(1,0)))
        key(KEY_LEFT,  10, onKeyDown = moveIfFreeLocation(trace, Vec(-1,0)))

        /*leftMouse(onBtnDown = {
          mouse_coord => tracer.updateLocation(trace, mouse_coord)
        })*/

        val poly = displayList {
          drawPolygon(Array(Vec(100, 300), Vec(150, 250), Vec(300, 300), Vec(300, 450), Vec(200, 400)), CYAN)
        }

        val stars = displayList {
          for(i <- 1 to 100) {
            drawPoint(Vec(math.random.toFloat*width, math.random.toFloat*height), randomColor)
          }
        }

        private var target_point = trace.coord
        mouseMotion {
          mouse_coord =>
            target_point = (mouse_coord - trace.coord).n * 20
        }

        val physics = new ScagePhysics
        action {
          physics.step()
        }
        leftMouse(onBtnDown = {
          mouse_coord => physics.addPhysical(new DynaBall(trace.coord, 2) {
            velocity = (mouse_coord - trace.coord).n*10
            render {
              if(isActive) drawFilledCircle(coord, 2, YELLOW)
            }

            val action_id:Int = action {
              if(!tracer.isCoordOnArea(coord)) {
                isActive = false
                delActionOperation(action_id)
              }
            }
          })
        })

        backgroundColor = BLACK
        interface {
          print(xml("hello.world"), width/2, height/2,    WHITE)
          print(trace.point,        width/2, height/2-20, WHITE)
          print(fps, 10, height-20, WHITE)
        }
        render {
          drawDisplayList(stars)
          drawFilledCircle(trace.coord, 10, RED)
          drawLine(trace.coord, trace.coord + target_point)
          drawCircle(another_trace.coord, 10, GREEN)
          drawDisplayList(poly)
        }

        /*startServer()
        action {
          clients.foreach(client => {
            if(client.hasNewIncomingData) {
              if(client.incomingData.has("quit")) stop()
              else client.send(client.incomingData)
            }
          })
        }
        exit {
          stopServer()
        }*/
      }.run()
      assertTrue(true)
    };
}