package su.msk.dunno.scage2.support

import java.io.{FileInputStream, FileNotFoundException}
import java.util.Properties
import org.apache.log4j.Logger

object ScageProperties {
  private val log = Logger.getLogger(this.getClass);

  private val properties:Properties = {
    if(properties == null) {
      val p:Properties = new Properties()
      try{p.load(new FileInputStream("scage-properties.txt"))}
      catch {
        case ex:FileNotFoundException =>
          log.debug("failed to load properties: scage-properties.txt not found")
          System.exit(0)
      }
      p
    }
    else properties
  }
  def get(key:String):String = {
    val s = properties.getProperty(key)
    log.debug("read property "+key+": "+s)
    s
  }
  def getInt(key:String):Int = {
    val i = Integer.valueOf(properties.getProperty(key)).intValue
    log.debug("read property "+key+": "+i)
    i
  }
  def getFloat(key:String):Float = {
    val f = java.lang.Float.valueOf(properties.getProperty(key)).floatValue
    log.debug("read property "+key+": "+f)
    f
  }
  def getBoolean(key:String):Boolean = {
    val b = properties.getProperty(key).equalsIgnoreCase("yes")
    log.debug("read property "+key+": "+b)
    b
  }
}