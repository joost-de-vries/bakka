package banking.domain

import java.util.Date

import scala.util.{Success, Try}

sealed trait Transaction

sealed trait AccountEvent {
  def time: Date

  def amount: Long

  /** apply the accountevent to the account */
  def updated(oldAccount:Account) = oldAccount.copy(history = this :: oldAccount.history)

  /** the amount that results from applying this transaction on the account given a previous amount */
  def amount(prevAmount: Long): Long
}

case class Deposit(time: Date, amount: Long) extends AccountEvent with Transaction {
  override def amount(prevAmount: Long): Long = prevAmount + amount
}

case class Withdrawal(time: Date, amount: Long) extends AccountEvent with Transaction {
  /** determines the new amount after this transaction for the account given the previous amount */
  override def amount(prevAmount: Long): Long = prevAmount - amount
}

//TODO extend transferfrom from withdrawal
case class TransferFrom(time: Date, amount: Long, toAccountNr: Long) extends AccountEvent {
  override def amount(prevAmount: Long): Long = prevAmount - amount
}

case class TransferTo(time: Date, amount: Long, fromAccountNr: Long) extends AccountEvent {
  override def amount(prevAmount: Long): Long = prevAmount + amount
}

class Transfer(from: TransferFrom, to: TransferTo) extends Transaction
