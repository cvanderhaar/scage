package su.msk.dunno.scage.main

import su.msk.dunno.scage.handlers.controller.Controller
import su.msk.dunno.scage.prototypes.Handler
import su.msk.dunno.scage.handlers.{AI, Physics, Idler, Renderer}
import org.apache.log4j.Logger
import su.msk.dunno.scage.support.ScageProperties

object Scage {
  private val log = Logger.getLogger(Scage.getClass);
  log.info("starting scage...")
  
  private var scage_handlers:List[Handler] = Nil
  def handlers = scage_handlers
  def setDefaultHandlers = {Controller; Physics; AI; Renderer; Idler;}
  def addHandler(h:Handler) = {
	  scage_handlers = h :: scage_handlers
	  log.info("loaded handler "+h.getClass.getName)
  }
  def addHandlers(h:List[Handler]) = {scage_handlers = h ::: scage_handlers}

  var on_pause:Boolean = false
  def switchPause() = on_pause = !on_pause
  
  private var is_running = false
  def isRunning = is_running
  def start = {
    Idler
    run
  }
  private def run = {
    scage_handlers.foreach(h => h.initSequence)
    is_running = true
    while(is_running) scage_handlers.foreach(h => h.actionSequence)
    scage_handlers.foreach(h => h.exitSequence)
    log.info("app was stopped")
  }
  def stop = is_running = false

  def main(args:Array[String]):Unit = {
    val app_classname = ScageProperties.stringProperty("app")
    log.info("starting app "+app_classname)
    Class.forName(app_classname).getField("MODULE$").get(null)
    start
  }
}