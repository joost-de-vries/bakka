package banking.actors


import akka.actor.{ActorLogging, _}
import banking.actors.AccountManagerActor.UnconfirmedTransferFailure
import banking.domain.{Account, AccountEvent, TransferFrom}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


object AccountActor {
  val TRANSFER_TIMEOUT = 100L
  
  def props(number: Long) = Props(new AccountActor(Account.newAccount(number)) )

  /** for testing purposes */
  def props(account: Account) = Props(new AccountActor(account))

  def name = "account-actor"

  sealed trait Command
  
  case class DepositRequest(amount: Long) extends Command

  case class WithdrawalRequest(amount: Long) extends Command

  case object GetBalanceRequest extends Command
  
  case class Balance(amount:Long)

  case class TransferToRequest(amount: Long, fromAccountNumber: Long) extends Command

  case class TransferFromRequest(amount: Long, toAccountNumber: Long, toAccount: ActorRef) extends Command

  val DoNotUnderstand = AccountManagerActor.DoNotUnderstand
}


class AccountActor(var account: Account) extends Accounting with Actor with Stash with ActorLogging{
  override def receive=receiveRequests

  override def handleSuccess(theSender: ActorRef)(event: AccountEvent) = updateAndRespond(theSender)(event)
}

/** This trait handles the core Account commands protocol.
  * The actor has two states: receiveRequests and transferring. The latter state is when 
  * the actor is waiting for confirmation from the other account that the transfer has been 
  * received. During the latter state other incoming requests are stashed. */
trait Accounting extends Stash with ActorLogging {
    this:Actor =>
    
    var account:Account
  import banking.actors.AccountActor._

  def receiveRequests: Receive = {
    case DepositRequest(amount) =>
      respond(account.depositEvent(amount))

    case WithdrawalRequest(amount) =>
      respond(account.withdrawEvent(amount))

    case msg@TransferFromRequest(amount, toAccNumber, toAccountActor) =>
      account.transferEvent(amount, toAccountNr = toAccNumber) match {
        case Success(transferFromEvent) =>
          toAccountActor ! TransferToRequest(amount, account.number)
          context.setReceiveTimeout(TRANSFER_TIMEOUT millis)
          context become transferring(transferFromEvent, sender(), msg)
        case Failure(e) =>
          sender() ! Status.Failure(e)
      }
    case TransferToRequest(amount, fromAccNumber) =>
      respond(account.receiveTransferEvent(amount, fromAccountNr = fromAccNumber))

    case GetBalanceRequest =>
      sender() ! Balance(account.balance)

    case msg: Any =>
      log.error(s"received unexpected message $msg")
      sender() ! DoNotUnderstand
  }

  /** Either handle a successful accountEvent or handle the failure. 
    * In case of failure the sender will be notified. *
    */
  def respond(eventTry:Try[AccountEvent],theSender:ActorRef=sender()) = eventTry match{
    case Success(event:AccountEvent) =>
        handleSuccess(theSender)(event)
    case Failure(e) =>
      theSender ! Status.Failure(e)
  }

    /** this will be implemented differently by a non persistent actor implementation or a persistent actor implementation */
  def handleSuccess(theSender:ActorRef)(event:AccountEvent)
  
    /** update the state and notify the original sender of success */
  def updateAndRespond(theSender:ActorRef)(event:AccountEvent)={
    update(event)
    theSender ! Balance(account.balance)
  }
  
  /** this function is used as a result of commands that have been received and as a result of recovering */
  def update(event:AccountEvent) ={
    account=event.updated(account)
  }

  /* waiting for a confirmation from the counter account
  * @param transferFrom the accountevent that we'll persist and apply if we get the expected confirmation
  * @param origSender the actor whom we'll notify of the outcome * */
  def transferring(transferFrom:TransferFrom, origSender: ActorRef, transferFromRequest: TransferFromRequest): Receive = {
      case Balance(_) =>
        respond(Success(transferFrom), origSender)
        unbecome()
      case error: Status.Failure =>
        origSender ! error
        unbecome()
      case ReceiveTimeout =>
        origSender ! s"timeout: did not receive confirmation of counter account within expected $TRANSFER_TIMEOUT millis"
        context.parent ! UnconfirmedTransferFailure(fromAccountNr = account.number, toAccountNr = transferFromRequest.toAccountNumber, msg = transferFromRequest)
        unbecome()
      case a@_ if receive.isDefinedAt(a) => //stash messages intended for original receiveCommand state
        context.setReceiveTimeout(context.receiveTimeout)
        stash()
      case msg: Any =>
        log.error(s"received unexpected message $msg")
        sender() ! DoNotUnderstand
    }

    private def unbecome() = {
      context.setReceiveTimeout(Duration.Undefined)
      unstashAll()
      context unbecome()
    }
}

