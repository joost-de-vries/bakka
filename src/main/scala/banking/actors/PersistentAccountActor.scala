package banking.actors

import akka.actor.{ActorRef, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import banking.domain.{Account, AccountEvent}

class PersistentAccountActor(var account: Account) extends Accounting with PersistentActor {
  override def persistenceId = context.self.path.name
  
  override def receiveCommand =receiveRequests

  override def handleSuccess(theSender: ActorRef)(event: AccountEvent) = persist(event) { event2 =>
    updateAndRespond(theSender)(event2)
    context.system.eventStream.publish(event)
  }

  override def receiveRecover: Receive = {
    case event: AccountEvent => log.info(s"recovering $event")
      update(event)
    case t: RecoveryCompleted =>
      log.info(s"recovery completed")
    case SnapshotOffer(_, newAccount: Account) =>
      log.info(s"recovery: got snapshot: $newAccount")
      account = newAccount
  }
}

object PersistentAccountActor {
  def props(number: Long) = Props(new PersistentAccountActor(Account.newAccount(number)))

  /** for testing purposes */
  def props(account: Account) = Props(new PersistentAccountActor(account))
}




