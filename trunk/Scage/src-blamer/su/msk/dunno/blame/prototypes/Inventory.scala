package su.msk.dunno.blame.prototypes

import collection.mutable.HashMap
import su.msk.dunno.screens.handlers.Renderer
import su.msk.dunno.scage.support.messages.ScageMessage
import org.lwjgl.input.Keyboard
import su.msk.dunno.screens.prototypes.ScageRender
import su.msk.dunno.screens.ScageScreen
import su.msk.dunno.blame.field.FieldObject
import su.msk.dunno.blame.support.BottomMessages

class Inventory(val owner:Living) {
  private var items:HashMap[String, List[FieldObject]] = new HashMap[String, List[FieldObject]]()
  private var item_positions:List[(String)] = Nil
    
  private var item_selector:Int = -1
  def itemSelector:Int = item_selector
  def itemSelector_= (new_num:Int):Unit = {
    if((new_num >= 1 && new_num <= item_positions.size) || new_num == -1) {
      item_selector = new_num
      if(is_item_selection) inventory_screen.stop
    }
  }
  
  def selectedItem:Option[FieldObject] = {
    if(item_selector >= 1 && item_selector <= item_positions.size &&
       !items(item_positions(item_selector-1)).isEmpty) Some(items(item_positions(item_selector-1)).head)
    else None
  }
  
  private var is_item_selection = false
  private var purpose = ""
  def selectItem(_purpose:String):Option[FieldObject] = {
    item_selector = -1
    is_item_selection = true
    purpose = _purpose
    inventory_screen.run
    selectedItem
  }
  
  def showInventory = {
    item_selector = -1
    is_item_selection = false
    inventory_screen.run
  }

  def addItem(item:FieldObject) = {
    if(items.keys.size < 10) {
      val name = item.getState.getString("name")
      if(items.contains(name)) items(name) = item :: items(name)
      else items += (name -> List(item))
      if(!item_positions.contains(name)) item_positions = name :: item_positions
    }
    else {
      //TODO: drop item on the ground!
    }
  }

  def removeItem(item:FieldObject) = {
    val name = item.getState.getString("name")
    if(items.contains(name)) {
      items(name) = items(name).tail
      if(items(name).size == 0) item_positions = item_positions.filterNot(_ == name)
    }
  }

  private lazy val inventory_screen = new ScageScreen("Inventory Screen") {
    private implicit def toObjectWithForeachI[A](l:List[A]) = new ScalaObject {
      def foreachi(func:(A, Int) => Unit) = {
        var index = 0
        l.foreach(value => {
          func(value, index)
          index += 1
        })
      }
    }

    addRender(new ScageRender {
      override def interface = {
        ScageMessage.print(ScageMessage.xml("inventory.ownership", owner.stat("name")), 20, Renderer.height-20)
        if(is_item_selection)
          ScageMessage.print(purpose, 20, Renderer.height-20-ScageMessage.row_height)
        if(item_selector == -1) {
          item_positions.foreachi((key, i) => {
            ScageMessage.print((i+1) + ". " + key + " (" + items(key).size+")",
              20, Renderer.height-ScageMessage.row_height*4-i*ScageMessage.row_height, items(key).head.getColor)
          })
        }
        else if(item_selector >= 1 && item_selector <= item_positions.size &&
                !items(item_positions(item_selector-1)).isEmpty) {
          val selected_item = items(item_positions(item_selector-1)).head
          ScageMessage.print(selected_item.getState.getString("name")+":\n"+
                             selected_item.getState.getString("description"), 20, Renderer.height-60, selected_item.getColor)
        }
        if(is_item_selection) {
          ScageMessage.print(ScageMessage.xml("inventory.selection.helpmessage"),
            10, BottomMessages.bottom_messages_height - ScageMessage.row_height)
        }
        else ScageMessage.print(ScageMessage.xml("inventory.show.helpmessage"),
              10, BottomMessages.bottom_messages_height - ScageMessage.row_height)
      }
    })

    keyListener(Keyboard.KEY_1, onKeyDown = itemSelector = 1)
    keyListener(Keyboard.KEY_2, onKeyDown = itemSelector = 2)
    keyListener(Keyboard.KEY_3, onKeyDown = itemSelector = 3)
    keyListener(Keyboard.KEY_4, onKeyDown = itemSelector = 4)
    keyListener(Keyboard.KEY_5, onKeyDown = itemSelector = 5)
    keyListener(Keyboard.KEY_6, onKeyDown = itemSelector = 6)
    keyListener(Keyboard.KEY_7, onKeyDown = itemSelector = 7)
    keyListener(Keyboard.KEY_8, onKeyDown = itemSelector = 8)
    keyListener(Keyboard.KEY_9, onKeyDown = itemSelector = 9)
    keyListener(Keyboard.KEY_0, onKeyDown = itemSelector = 10)

    keyListener(Keyboard.KEY_NUMPAD1, onKeyDown = itemSelector = 1)
    keyListener(Keyboard.KEY_NUMPAD2, onKeyDown = itemSelector = 2)
    keyListener(Keyboard.KEY_NUMPAD3, onKeyDown = itemSelector = 3)
    keyListener(Keyboard.KEY_NUMPAD4, onKeyDown = itemSelector = 4)
    keyListener(Keyboard.KEY_NUMPAD5, onKeyDown = itemSelector = 5)
    keyListener(Keyboard.KEY_NUMPAD6, onKeyDown = itemSelector = 6)
    keyListener(Keyboard.KEY_NUMPAD7, onKeyDown = itemSelector = 7)
    keyListener(Keyboard.KEY_NUMPAD8, onKeyDown = itemSelector = 8)
    keyListener(Keyboard.KEY_NUMPAD9, onKeyDown = itemSelector = 9)
    keyListener(Keyboard.KEY_NUMPAD0, onKeyDown = itemSelector = 10)

    keyListener(Keyboard.KEY_ESCAPE, onKeyDown = {
      if(itemSelector != -1) itemSelector = -1
      else stop
    })
  }
}