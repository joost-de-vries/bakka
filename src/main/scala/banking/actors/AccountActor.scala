package banking.actors


import java.util.UUID

import akka.actor._
import akka.actor.ActorLogging
import banking.actors.AccountManagerActor.DoNotUnderstand
import banking.domain.Account

import scala.util.{Try, Failure, Success}

object AccountActor {
  def props(number: Long) = Props(new AccountActor(Account.newAccount(number)))

  def props(account: Account) = Props(new AccountActor(account))

  def name = "simple-account-actor"

  case class DepositRequest(amount: Long)

  case class WithdrawalRequest(amount: Long)

  case object GetBalanceRequest

  case class TransferToRequest(amount: Long, fromAccountNumber: Long)

  case class TransferFromRequest(amount: Long, toAccountNumber: Long, toAccount: ActorRef)
}

class AccountActor(var account: Account) extends Actor with ActorLogging {

  import AccountActor._

  override def receive: Receive = {
    case DepositRequest(amount) => {
      update(account.deposit(amount))
    }
    case WithdrawalRequest(amount) => {
      update(account.withdraw(amount))
    }
    case TransferFromRequest(amount, toAccNumber, toAccountActor) => {
      account.transfer(amount, toAccountNr = toAccNumber) match {
        case Success(newAccount) =>
          toAccountActor ! TransferToRequest(amount, account.number)
          context become transfering(newAccount, sender())
        case Failure(e) =>
          sender() ! e.getMessage()
      }
    }
    case TransferToRequest(amount, fromAccNumber) => {
      update(account.receiveTransfer(amount, fromAccountNr = fromAccNumber))
    }
    case GetBalanceRequest => {
      sender() ! account.balance
    }
    case a@_ => {
      log.error(s"${getClass().getName} received unexpected message $a")
      sender() ! DoNotUnderstand.message
    }
  }

  /** update account or send error message */
  private def update(accTry: Try[Account]) = {
    accTry match {
      case Success(newAccount) =>
        account = newAccount
        sender() ! account.balance
      case Failure(e) => sender() ! e.getMessage()
    }
  }

  private def transfering(newAccount: Account, origSender: ActorRef): Receive = {
    case amount: Long =>
      account = newAccount
      origSender ! newAccount.balance
      context unbecome()
    case error: String =>
      origSender ! error
      context unbecome()
    case a@_ => {
      log.error(s"${getClass().getName} received unexpected message $a")
      sender() ! DoNotUnderstand.message
    }
  }
}