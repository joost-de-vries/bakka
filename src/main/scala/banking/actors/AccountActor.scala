package banking.actors


import akka.actor.{ActorLogging, _}
import banking.actors.AccountManagerActor.{DoNotUnderstand, UnconfirmedTransferFailure}
import banking.domain.Account

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


object AccountActor {
  val TRANSFER_TIMEOUT = 100L
  
  def props(number: Long) = Props(new AccountActor(Account.newAccount(number)))

  def props(account: Account) = Props(new AccountActor(account))

  def name = "simple-account-actor"

  case class DepositRequest(amount: Long)

  case class WithdrawalRequest(amount: Long)

  case object GetBalanceRequest

  case class TransferToRequest(amount: Long, fromAccountNumber: Long)

  case class TransferFromRequest(amount: Long, toAccountNumber: Long, toAccount: ActorRef)

}

class AccountActor(var account: Account) extends Actor with Stash with ActorLogging {

  import banking.actors.AccountActor._

  override def receive: Receive = {
    case DepositRequest(amount) =>
      update(account.deposit(amount))

    case WithdrawalRequest(amount) =>
      update(account.withdraw(amount))

    case msg@TransferFromRequest(amount, toAccNumber, toAccountActor) =>
      account.transfer(amount, toAccountNr = toAccNumber) match {
        case Success(newAccount) =>
          toAccountActor ! TransferToRequest(amount, account.number)
          context.setReceiveTimeout(TRANSFER_TIMEOUT millis)
          context become transferring(newAccount, sender(), msg)
        case Failure(e) =>
          sender() ! e.getMessage
      }
    case TransferToRequest(amount, fromAccNumber) =>
      update(account.receiveTransfer(amount, fromAccountNr = fromAccNumber))

    case GetBalanceRequest =>
      sender() ! account.balance

  }

  /** update account or send error message */
  private def update(accTry: Try[Account]) = {
    accTry match {
      case Success(newAccount) =>
        account = newAccount
        sender() ! account.balance
      case Failure(e) => sender() ! e.getMessage
    }
  }

  /* waiting for a confirmation from the counter account
  * @param newAccount the new state of our account if the transfer is successful
  * @param origSender the actor whom we'll notify of the outcome * */
  private def transferring(newAccount: Account, origSender: ActorRef, transferFromRequest: TransferFromRequest): Receive = {
    case amount: Long =>
      account = newAccount
      origSender ! newAccount.balance
      context.setReceiveTimeout(Duration.Undefined)
      unstashAll()
      context unbecome()
    case error: String =>
      origSender ! error
      context.setReceiveTimeout(Duration.Undefined)
      unstashAll()
      context unbecome()
    case ReceiveTimeout =>
      origSender ! s"timeout: did not receive confirmation of counter account within expected $TRANSFER_TIMEOUT millis"
      context.parent ! UnconfirmedTransferFailure(fromAccountNr = account.number, toAccountNr = transferFromRequest.toAccountNumber, msg = transferFromRequest)
      context.setReceiveTimeout(Duration.Undefined)
      unstashAll()
      context unbecome()
    case a@_ if receive.isDefinedAt(a) => //stash messages intended for original receive
      context.setReceiveTimeout(context.receiveTimeout)
      stash()
  }

  override def preRestart(reason: scala.Throwable, message: scala.Option[scala.Any]) = {
    log.debug(s"${getClass.getName} $account restarting")
    super.preRestart(reason, message)
  }

  override def unhandled(msg: Any) {
    sender() ! DoNotUnderstand.message
    throw new RuntimeException(s"unhandled message ${account.number} $msg")
  }
}