package com.redis

object Pub {
  println("starting publishing service ..")
  val r = new RedisClient("localhost", 6379)
  val p = new Publisher(r)
  p.start

  def publish(channel: String, message: String) = {
    p ! Publish(channel, message)
  }
}

object Sub {
  println("starting subscription service ..")
  val r = new RedisClient("localhost", 6379)
  val s = new Subscriber(r)
  s.start
  s ! Register(callback) 

  def sub(channels: String*) = {
    s ! Subscribe(channels.toArray)
  }

  def unsub(channels: String*) = {
    s ! Unsubscribe(channels.toArray)
  }

  def callback(pubsub: PubSubMessage) = pubsub match {
    case S(channel, no) => println("subscribed to " + channel + " and count = " + no)
    case U(channel, no) => println("unsubscribed from " + channel + " and count = " + no)
    case M(channel, msg) => 
      msg match {
        // exit will unsubscribe from all channels and stop subscription service
        case "exit" => 
          println("unsubscribe all ..")
          r.unsubscribe

        // message "+x" will subscribe to channel x
        case x if x startsWith "+" => 
          val s: Seq[Char] = x
          s match {
            case Seq('+', rest @ _*) => r.subscribe(rest.toString){ m => }
          }

        // message "-x" will unsubscribe from channel x
        case x if x startsWith "-" => 
          val s: Seq[Char] = x
          s match {
            case Seq('-', rest @ _*) => r.unsubscribe(rest.toString)
          }

        // other message receive
        case x => 
          println("received message on channel " + channel + " as : " + x)
      }
  }
}
