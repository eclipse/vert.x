package org.vertx.scala.examples.echo

import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.net.NetSocket
import org.vertx.java.deploy.Verticle
import org.vertx.scala.core.NetSockets

class EchoClient extends Verticle with NetSockets {

  def start() {
    vertx.createNetClient.connect(1234, "localhost", { socket: NetSocket =>
      socket.dataHandler(new Handler[Buffer] {
        def handle(buffer: Buffer) {
          System.out.println("Net client receiving: " + buffer)
        }
      })

      for (i <- 0 until 10) {
        val str = "hello" + i + "\n"
        println("Net client sending: " + str)
        socket.write(new Buffer(str))
      }
    })
  }
}

