package net.scage.support.net

import _root_.net.scage.support.ScageProperties._
import java.io.{InputStreamReader, OutputStreamWriter, BufferedReader, PrintWriter}
import java.net.{SocketException, Socket}
import net.scage.support.State
import com.weiglewilczek.slf4s.Logger
import actors.Actor._
import net.scage.Scage

object NetClient extends NetClient(
  server_url =  property("net.server", "localhost"),
  port = property("net.port", 9800),
  ping_timeout = property("net.ping_timeout", 60000, {ping_timeout:Int => (ping_timeout >= 1000, "must be more than 1000")})
)

class NetClient(
  val server_url:String =  property("net.server", "127.0.0.1"),
  val port:Int = property("net.port", 9800),
  val ping_timeout:Int = property("net.ping_timeout", 60000, {ping_timeout:Int => (ping_timeout >= 1000, "must be more than 1000")})
) {
  private val log = Logger(this.getClass.getName)

  private var is_connected = false
  private var write_error  = false

  private val io_actor = actor {
    var onServerDataReceived:State => Any = state => {}
    var socket:Socket = null
    var out:PrintWriter = null
    var in:BufferedReader = null
    loopWhile(Scage.isAppRunning) {
      react {
        case ("connect", new_onServerDataReceived:(State => Any))=>
          if(isServerOnline) {
            is_connected = false
            if(socket != null) {
              val socket_url = socket.getInetAddress.getHostAddress
              socket.close()
              log.info("disconnected from server "+socket_url)
            }
          }
          log.info("start connecting to server "+server_url+" at port "+port)
          socket = try {new Socket(server_url, port)}
          catch {
            case e:java.io.IOException => {
              log.error("failed to connect to server "+server_url+" at port "+port+": "+e);
              null
            }
          }
          if(socket != null) {
            onServerDataReceived = new_onServerDataReceived
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream, "UTF-8"))
            in = new BufferedReader(new InputStreamReader(socket.getInputStream, "UTF-8"))
            is_connected = true
            write_error = false
            log.info("connected!")
          }
        case ("send", data:State) =>
          log.debug("sending data to server:\n"+data)
          if(isServerOnline) {
            out.println(data.toJsonString)
            out.flush()
            write_error = out.checkError()
            if(write_error) log.warn("failed to send data to server: write error!")
          } else log.warn("not connected to send data!")
        case "receive" =>
          if(is_connected) {
            if(in.ready) {
              try {
                val message = in.readLine
                log.debug("incoming message from server:\n"+message)
                val received_data = State.fromJsonStringOrDefault(message, State(("raw" -> message)))
                if(received_data.contains("ping")) log.debug("received ping from server")
                else {
                  log.debug("received data from server:\n"+received_data)
                  actor {
                    onServerDataReceived(received_data)
                  }
                }
              } catch {
                case e:Exception => {
                  log.error("error while receiving data from server: "+e)
                  // disconnect maybe?
                }
              }
            }
          } // else maybe?
        case "disconnect" =>
          is_connected = false
          if(socket != null) {
            val socket_url = socket.getInetAddress.getHostAddress
            socket.close()
            log.info("disconnected from server "+socket_url)
          }
      }
    }
  }

  def send(data:State) {
    io_actor ! ("send", data)
  }

  def send(data:String) {send(State(("raw" -> data)))}

  def isServerOnline = is_connected && !write_error

  private var is_running = false
  def startClient(onServerDataReceived:(State) => Any = (state) => {}) {
    is_running = true
    actor {
      var last_ping_moment = System.currentTimeMillis()
      while(is_running) {
        if(!isServerOnline) { // connection checker
          Thread.sleep(1000)
          io_actor ! ("connect", onServerDataReceived)
        } else {
          Thread.sleep(10)
          io_actor ! "receive"
          if(System.currentTimeMillis() - last_ping_moment > ping_timeout) {
            io_actor ! ("send", State("ping"))
            last_ping_moment = System.currentTimeMillis()
          }
        }
      }
      io_actor ! "disconnect"
    }
  }

  def stopClient() {
    is_running = false
  }
}