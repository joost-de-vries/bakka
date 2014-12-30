package banking.actors

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import banking.actors.AccountActor.AccountRequest
import banking.domain.InsufficientFunds

object AccountManagerActor {
  def props = Props[AccountManagerActor]
  def name = "account-manager-actor"

  case class Envelope[T](accountNumber: Long, payload: T)
  case class TransferRequest(amount:Long,toAccountNumber:Long)

  object NotAllowed {
    val message="not allowed"
  }
  object DoNotUnderstand {
    val message="do not understand"
  }
}

class AccountManagerActor() extends Actor with ActorCreation with ActorLogging {
  import AccountManagerActor._
  import AccountActor._
  
  var transferRqSenders = Map.empty[ActorRef,ActorRef]

  def fromOutstandingRqSender(senderRef: ActorRef) = {
    transferRqSenders.contains(senderRef)
  }

  override def receive: Receive =     {

      case Envelope(_, TransferToRequest(_, _)) | 
           Envelope(_, TransferFromRequest(_, _) |
           Envelope(_, AccountRequest)) =>
        sender() ! NotAllowed.message

      case Envelope(accountNumber, TransferRequest(amount, toAccountNumber)) => {
        val fromAccountRef = getOrCreateChild(props = AccountActor.props(accountNumber), name = accountNumber.toString)
        val toAccountRef = getOrCreateChild(props = AccountActor.props(toAccountNumber), name = toAccountNumber.toString)
        fromAccountRef ! TransferFromRequest(amount = amount, toAccountNumber = toAccountNumber)
        toAccountRef ! TransferToRequest(amount = amount, fromAccountNumber = accountNumber)
        val s = sender()
        transferRqSenders+=( fromAccountRef -> s)
      }
      case amount: Long if fromOutstandingRqSender(sender())=> {
        val origSender = transferRqSenders(sender())
        origSender ! amount
        transferRqSenders-=origSender
      }
      case _:Long if !fromOutstandingRqSender(sender())=> //ignore
      case Envelope(accountNumber, payload) =>
        getOrCreateChild(props = AccountActor.props(accountNumber), name = accountNumber.toString) forward payload

      case error:String if error==InsufficientFunds.message && fromOutstandingRqSender(sender())=> ??? //now what?
      case a@_ => {
        log.error(s"actormanager doesn't understand $a")
        sender() ! DoNotUnderstand
      }
    }
  

}
