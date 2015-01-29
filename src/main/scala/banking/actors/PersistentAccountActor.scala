package banking.actors

import akka.actor.{Props, ActorRef}
import akka.persistence.PersistentActor
import banking.domain.{AccountEvent, Account}

class PersistentAccountActor(var account:Account) extends Accounting with AccountingPersistency with PersistentActor{
  override def receiveCommand =receiveRequests

  override def handleSuccess(theSender: ActorRef)(event: AccountEvent) = persist(event) { event2 =>
    updateAndRespond(theSender)(event2)
    context.system.eventStream.publish(event)
  }
}

object PersistentAccountActor {
  def props(number: Long) = Props(new PersistentAccountActor(Account.newAccount(number)))

  /** for testing purposes */
  def props(account: Account) = Props(new PersistentAccountActor(account))
}