package banking

import java.util.Date

import scala.util.{Failure, Success, Try}

sealed trait Transaction {
  def time: Date

  def amount: Long

  def valid: Try[Unit]

  /** the amount that results from applying this transaction on the account given a previous amount */
  def amount(account: Account, prevAmount: Long): Long
}

case class Deposit(time: Date, amount: Long, to: Account) extends Transaction {
  override val valid:Success[Unit] = Success(())

  override def amount(account: Account, prevAmount: Long): Long = prevAmount + amount
}

case class Withdrawal(time: Date, amount: Long, from: Account) extends Transaction {
  override def valid = if (from.balance >= amount) Success(()) else InsufficientFunds()

  /** determines the new amount after this transaction for the account given the previous amount */
  override def amount(account: Account, prevAmount: Long): Long = prevAmount - amount

}

case class Transfer(time: Date, amount: Long, from: Account, to: Account) extends Transaction {
  override def valid = if (from.balance >= amount) Success(()) else InsufficientFunds()

  override def amount(account: Account, prevAmount: Long): Long = {
    if (from.number == account.number) prevAmount - amount
    else if (to.number == account.number) prevAmount + amount
    else throw new IllegalStateException()
  }
}

case class Account(number: Long, history: List[Transaction]) {
  lazy val balance:Long = balance(new Date())

  def balance(date: Date):Long = {
    history.dropWhile(tx => tx.time.after(date))
      .foldLeft(0L) { (acc, tx) =>
        tx.amount(account = this, prevAmount = acc)
    }
  }
}

object Account {

  def withdraw(from: Account, amount: Long,time:Date=new Date()): Try[Account] = {
    val tx = Withdrawal(time, amount, from = from)
    tx.valid.map { _ => from.copy(history = tx :: from.history)}
  }

  def deposit(to: Account, amount: Long,time:Date=new Date()): Try[Account] = {
    val tx = Deposit(time, amount, to = to)
    tx.valid.map(_ => to.copy(history = tx :: to.history))
  }

  /** if successful returns a pair of the new from and to accounts */
  def transfer(amount: Long, from: Account, to: Account,time:Date=new Date()): Try[(Account, Account)] = {
    val tx = Transfer(time, amount, from = from, to = to)
    tx.valid.map(_ => (from.copy(history = tx :: from.history), to.copy(history = tx :: to.history)))
  }

  def newAccount(number: Long): Account = Account(number, Nil)
}

object InsufficientFunds {
  def apply() = Failure(new RuntimeException("insufficient funds"))

}

