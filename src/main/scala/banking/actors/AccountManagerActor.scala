package banking.actors

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
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
  

  override def receive: Receive =     {

      case Envelope(_, TransferToRequest(_, _)) |
           Envelope(_, TransferFromRequest(_, _, _)) =>
        sender() ! NotAllowed.message

      case Envelope(accountNumber, TransferRequest(amount, toAccountNumber)) => {
        val fromAccountRef = getOrCreateChild(props = AccountActor.props(accountNumber), name = accountNumber.toString)
        val toAccountRef = getOrCreateChild(props = AccountActor.props(toAccountNumber), name = toAccountNumber.toString)
        fromAccountRef forward TransferFromRequest(amount = amount, toAccountNumber = toAccountNumber, toAccountRef)
      }
      case Envelope(accountNumber, payload) =>
        getOrCreateChild(props = AccountActor.props(accountNumber), name = accountNumber.toString) forward payload

      case a@_ => {
        log.error(s"${getClass().getName} received unexpected message $a")
        sender() ! DoNotUnderstand
      }
    }
  

}
