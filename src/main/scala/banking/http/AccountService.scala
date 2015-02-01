package banking.http

import akka.actor.ActorRef
import akka.pattern.ask
import banking.actors.AccountActor.{Balance, DepositRequest, GetBalanceRequest, WithdrawalRequest}
import banking.actors.AccountManagerActor.{Envelope, TransferRequest}
import banking.http.Main._

import scala.concurrent.Future

class AccountService(accountManagerActor: ActorRef) {

  def balance(accountNumber: Long): Future[Balance] =
    (accountManagerActor ? Envelope(accountNumber = accountNumber, GetBalanceRequest)).mapTo[Balance]

  def deposit(accountNumber: Long, amount: Long): Future[Balance] =
    (accountManagerActor ? Envelope(accountNumber = accountNumber, DepositRequest(amount))).mapTo[Balance]

  def withdraw(accountNumber: Long, amount: Long): Future[Balance] =
    (accountManagerActor ? Envelope(accountNumber = accountNumber, WithdrawalRequest(amount))).mapTo[Balance]

  def transfer(fromAccountNumber: Long, amount: Long, toAccountNumber: Long): Future[Balance] =
    (accountManagerActor ? Envelope(accountNumber = fromAccountNumber, TransferRequest(amount = amount, toAccountNumber = toAccountNumber))).mapTo[Balance]
}
