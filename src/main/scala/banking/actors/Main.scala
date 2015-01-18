package banking.actors

import akka.actor.ActorSystem
import banking.actors.AccountManagerActor.Envelope
import banking.actors.AccountActor.DepositRequest

/**
 * Created by j.de.vries on 29-12-2014.
 */
object Main extends App{
  implicit val system = ActorSystem("webapi")

  // create and start our service actor
  val service = system.actorOf(AccountManagerActor.props, AccountManagerActor.name)

  //service. ! AccountManagerActor.Envelope(100L,DepositRequest(50L))
}
