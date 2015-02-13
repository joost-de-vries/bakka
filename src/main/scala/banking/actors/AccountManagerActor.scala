package banking.actors

import akka.actor.{Actor, ActorLogging, Props, Status}
import banking.actors.AccountActor.{TransferFromRequest, TransferToRequest}

object AccountManagerActor {
  def props = Props[AccountManagerActor]
  def name = "account-manager-actor"

  case class Envelope[T](accountNumber: Long, payload: T)
  case class TransferRequest(amount:Long,toAccountNumber:Long)

  object NotAllowed {
    val message = Status.Failure(new IllegalAccessException())
  }
  object DoNotUnderstand {
    val message = Status.Failure(new UnsupportedOperationException())
  }

  class UnconfirmedTransferException(val fromAccountNr: Long, val toAccountNr: Long, val request: TransferFromRequest) extends Exception

  object UnconfirmedTransferFailure {
    def apply(fromAccountNr: Long, toAccountNr: Long, msg: TransferFromRequest) = Status.Failure(new UnconfirmedTransferException(fromAccountNr, toAccountNr, msg))

    def unapply(failure: Status.Failure) = failure.cause match {
      case e: UnconfirmedTransferException => Some((e.fromAccountNr, e.toAccountNr, e.request))
      case _ => None
    }
  }
}

class AccountManagerActor() extends Actor with ActorCreation with ActorLogging {
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
