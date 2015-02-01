package banking.actors

import akka.actor.{Actor, ActorLogging, Props}
import banking.actors.AccountActor.TransferFromRequest

object AccountManagerActor {
  def props = Props[AccountManagerActor]
  def name = "account-manager-actor"

  case class Envelope[T](accountNumber: Long, payload: T)
  case class TransferRequest(amount:Long,toAccountNumber:Long)

  case class UnconfirmedTransferFailure(fromAccountNr: Long, toAccountNr: Long, msg: TransferFromRequest)

  object NotAllowed {
    val message="not allowed"
  }
  object DoNotUnderstand {
    val message="do not understand"
  }
}

class AccountManagerActor() extends Actor with ActorCreation with ActorLogging {
  import banking.actors.AccountActor._
  import banking.actors.AccountManagerActor._

  override def receive: Receive =     {

      case Envelope(_, TransferToRequest(_, _)) |
           Envelope(_, TransferFromRequest(_, _, _)) =>
        sender() ! NotAllowed.message

      case Envelope(accountNumber, TransferRequest(amount, toAccountNumber)) => 
        val fromAccountRef = getOrCreateChild(props = AccountActor.props(accountNumber), name = accountNumber.toString)
        val toAccountRef = getOrCreateChild(props = AccountActor.props(toAccountNumber), name = toAccountNumber.toString)
        fromAccountRef forward TransferFromRequest(amount = amount, toAccountNumber = toAccountNumber, toAccountRef)

      case Envelope(accountNumber, payload: AccountActor.Command) =>
        getOrCreateChild(props = AccountActor.props(accountNumber), name = accountNumber.toString) forward payload

      case error@UnconfirmedTransferFailure(fromAccountNr, toAccountNr, transferFromRequest) =>
        log.error(s"did not receive transfer confirmation $error")
      case a@_ =>
        log.error(s"${getClass.getName} received unexpected message $a")
        sender() ! DoNotUnderstand
    }
}
