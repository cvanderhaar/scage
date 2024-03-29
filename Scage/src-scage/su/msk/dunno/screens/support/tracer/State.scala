package su.msk.dunno.screens.support.tracer

import collection.mutable.HashMap
import su.msk.dunno.scage.support.{ScageColor, Vec}
import su.msk.dunno.scage.support.ScageColors._

class State() {
  private val args:HashMap[String, StateData] = new HashMap[String, StateData]()

  def this(key:String) = {this(); args += key -> new StateData()}
  def put(key:String) = {
    args += key -> new StateData()
    this
  }

  def this(key:String, float_num:Float) = {this(); args += key -> new StateData(float_num)}
  def put(key:String, float_num:Float):State = {
    if(args.contains(key)) args(key).float = float_num
    else args += key -> new StateData(float_num);
    this
  }
  def getFloat(key:String):Float = {
    if(!args.contains(key)) 0
    else if(args(key).float != 0) args(key).float
    else 0
  }
  def getInt(key:String):Int = getFloat(key).toInt

  def this(key:String, message:String) = {this(); args += key -> new StateData(message)}
  def put(key:String, message:String):State = {
    if(args.contains(key)) args(key).string = message
    else args += key -> new StateData(message);
    this
  }
  def getString(key:String) = {
    if(!args.contains(key)) ""
    else args(key).string
  }
  def getNumAsString(key:String) = {
    if(!args.contains(key)) ""
    else {
      val float_num = args(key).float
      val int_num = args(key).float.toInt
      if(int_num == float_num) int_num.toString
      else float_num.toString
    }
  }

  def this(key:String, vec:Vec) = {this(); args += key -> new StateData(vec)}
  def put(key:String, new_vec:Vec):State = {
    if(args.contains(key)) args(key).vec = new_vec
    else args += key -> new StateData(new_vec);
    this
  }
  def getVec(key:String) = {
    if(!args.contains(key)) Vec(0,0)
    else args(key).vec
  }

  def this(key:String, b:Boolean) = {this(); args += key -> new StateData(b)}
  def put(key:String, b:Boolean):State = {
    if(args.contains(key)) args(key).bool = b
    else args += key -> new StateData(b);
    this
  }
  def getBool(key:String) = {
    if(!args.contains(key)) false
    else args(key).bool
  }

  def this(key:String, col:ScageColor) = {this(); args += key -> new StateData(col)}
  def put(key:String, col:ScageColor):State = {
    if(args.contains(key)) args(key).color = col
    else args += key -> new StateData(col);
    this
  }
  def getColor(key:String) = {
    if(!args.contains(key)) BLACK
    else args(key).color
  }

  def contains(key:String):Boolean = args.contains(key)

  private[State] class StateData() {
	  private var f = 0.0f
	  def this(f:Float) = {this(); this.f = f;}
	  def float = f
    def float_= (new_f:Float) = f = new_f

	  private var s = ""
	  def this(s:String) = {this(); this.s = s;}
	  def string = s
    def string_= (new_s:String) = s = new_s

	  private var v = Vec(0,0)
	  def this(v:Vec) = {this(); this.v = v;}
	  def vec = v
    def vec_= (new_v:Vec) = v = new_v

    private var b = false
    def this(b:Boolean) = {this(); this.b = b;}
    def bool = b
    def bool_= (new_b:Boolean) = b = new_b

    private var col = BLACK
    def this(col:ScageColor) = {this(); this.col = col;}
    def color = col
    def color_= (new_col:ScageColor) = col = new_col
  }
}