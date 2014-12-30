package banking.actors

import akka.actor.{ActorContext, Props, ActorRef}

/**
 * Created by j.de.vries on 29-12-2014.
 */
trait ActorCreation {
  def getOrCreateChild(props: Props, name: String): ActorRef = getChild(name).getOrElse(createChild(props, name))

  def context: ActorContext

  def getChild(name: String): Option[ActorRef] = context.child(name)
  def createChild(props: Props, name: String): ActorRef = context.actorOf(props, name)
}
