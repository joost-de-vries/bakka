package banking.domain

import java.util.Date

import scala.util.{Success, Failure, Try}


case class Account(number: Long, history: List[AccountEvent]) {

  lazy val balance: Long = balance(history)

  def balance(date: Date): Long = balance(history.dropWhile(tx => tx.time.after(date)))

  private def balance(forHistory: List[AccountEvent]) = forHistory.foldRight(0L) { (tx, acc) =>
    tx.amount(prevAmount = acc)
  }

  def withdrawEvent(amount: Long, time: Date = new Date()): Try[Withdrawal] = {
    if (balance >= amount) Success(Withdrawal(time, amount)) else InsufficientFunds()
  }

  def withdraw(amount: Long, time: Date = new Date()): Try[Account] = {
    withdrawEvent(amount,time) map {tx => tx.updated(this)}
  }
  
  def depositEvent(amount: Long, time: Date = new Date()): Try[Deposit] = {
    Success(Deposit(time, amount))
  }

  def deposit(amount: Long, time: Date = new Date()): Try[Account] = {
    depositEvent(amount,time) map { tx => tx.updated(this)}
  }
  
  def transferEvent(amount: Long, toAccountNr: Long, time: Date = new Date()): Try[TransferFrom]={
    if (balance >= amount) Success(TransferFrom(time, amount, toAccountNr = toAccountNr)) else InsufficientFunds()
  }
  def transfer(amount: Long, toAccountNr: Long, time: Date = new Date()): Try[Account] = {
    transferEvent(amount,toAccountNr,time) map {tx => tx.updated(this)}
  }

  def receiveTransferEvent(amount: Long, fromAccountNr: Long, time: Date = new Date()): Try[TransferTo] = {
    Success( TransferTo(time, amount, fromAccountNr = fromAccountNr))
  }
  def receiveTransfer(amount: Long, fromAccountNr: Long, time: Date = new Date()): Try[Account] = {
    receiveTransferEvent(amount,fromAccountNr,time) map {tx => tx.updated(this)}
  }
}

object Account {
  /** if successful returns a pair of the new from and to accounts */
  def transfer(amount: Long, from: Account, to: Account, time: Date = new Date()): Try[(Account, Account)] = {
    val fromTry = from.transfer(amount = amount, toAccountNr = to.number)
    val toTry = to.receiveTransfer(amount = amount, fromAccountNr = from.number)

    fromTry.flatMap(from =>
      toTry.map(to =>
        (from, to)
      )
    )
  }

  def newAccount(number: Long): Account = Account(number, Nil)
}

object InsufficientFunds {
  def apply() = Failure(new RuntimeException(message))

  val message = "insufficient funds"
}
