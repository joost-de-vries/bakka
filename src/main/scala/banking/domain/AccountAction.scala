package banking.domain

import java.util.Date

import scala.util.{Success, Try}

sealed trait Transaction

sealed trait AccountAction {
  def time: Date

  def amount: Long

  /** is this a valid transaction for the originating account? */
  def valid(account:Account): Try[Unit]

  /** the amount that results from applying this transaction on the account given a previous amount */
  def amount(prevAmount: Long): Long
}

case class Deposit(time: Date, amount: Long) extends AccountAction with Transaction {
  override def valid(account:Account):Success[Unit] = Success(())

  override def amount(prevAmount: Long): Long = prevAmount + amount
}

case class Withdrawal(time: Date, amount: Long) extends AccountAction with Transaction {
  override def valid(account:Account) = if (account.balance >= amount) Success(()) else InsufficientFunds()

  /** determines the new amount after this transaction for the account given the previous amount */
  override def amount(prevAmount: Long): Long = prevAmount - amount
}

//TODO extend transferfrom from withdrawal
case class TransferFrom(time: Date, amount: Long, toAccountNr: Long) extends AccountAction {
  override def valid(account:Account) = if (account.balance >= amount) Success(()) else InsufficientFunds()

  override def amount(prevAmount: Long): Long = prevAmount - amount
}

case class TransferTo(time: Date, amount: Long, fromAccountNr: Long) extends AccountAction {
  override def valid(account:Account) =  Success(())

  override def amount(prevAmount: Long): Long = {
    prevAmount + amount
  }
}

class Transfer(from: TransferFrom, to: TransferTo) extends Tuple2(from, to) with Transaction