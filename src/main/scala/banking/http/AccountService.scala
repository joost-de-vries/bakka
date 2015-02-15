package banking.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import banking.actors.AccountActor.{Balance, DepositRequest, GetBalanceRequest, WithdrawalRequest}
import banking.actors.AccountManagerActor.{Envelope, TransferRequest}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class AccountService(accountManagerActor: ActorRef) {
  implicit val timeout = new Timeout(500 millis)

  def balance(accountNumber: Long): Future[Balance] =
    (accountManagerActor ? Envelope(accountNumber = accountNumber, GetBalanceRequest)).mapTo[Balance]

  def deposit(accountNumber: Long, amount: Long): Future[Balance] =
    (accountManagerActor ? Envelope(accountNumber = accountNumber, DepositRequest(amount))).mapTo[Balance]

  def withdraw(accountNumber: Long, amount: Long): Future[Balance] =
    (accountManagerActor ? Envelope(accountNumber = accountNumber, WithdrawalRequest(amount))).mapTo[Balance]

  def transfer(fromAccountNumber: Long, amount: Long, toAccountNumber: Long): Future[Balance] =
    (accountManagerActor ? Envelope(accountNumber = fromAccountNumber, TransferRequest(amount = amount, toAccountNumber = toAccountNumber))).mapTo[Balance]
}
