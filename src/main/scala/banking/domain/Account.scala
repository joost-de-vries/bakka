package banking.domain

import java.util.Date

import scala.util.{Success, Failure, Try}


case class Account(number: Long, history: List[Transaction]) {

  lazy val balance:Long = balance(new Date())

  def balance(date: Date):Long = {
    history.dropWhile(tx => tx.time.after(date))
      .foldRight(0L) { (tx,acc) =>
        tx.amount(prevAmount = acc)
    }
  }
  def withdraw(amount: Long,time:Date=new Date()): Try[Account] = {
    val tx = Withdrawal(time, amount, from = this)
    tx.valid(this).map { _ => this.copy(history = tx :: history)}
  }

  def deposit(amount: Long,time:Date=new Date()): Try[Account] = {
    val tx = Deposit(time, amount, to = this)
    tx.valid(this).map(_ => this.copy(history = tx ::history))
  }
  
  def transfer(amount: Long, toAccountNr:Long,time:Date=new Date()): Try[Account] = {
    val tx = TransferFrom(time, amount, from = this, toAccountNr = toAccountNr)
    tx.valid(this).map(_ => this.copy(history = tx :: this.history))
  }
  def receiveTransfer(amount: Long, fromAccountNr:Long,time:Date=new Date()): Try[Account] = {
    val tx = TransferTo(time, amount, to = this, fromAccountNr = fromAccountNr)
    tx.valid(this).map(_ => this.copy(history = tx :: this.history))
  }
}

object Account {
  /** if successful returns a pair of the new from and to accounts */
  def transfer(amount: Long,from:Account, to: Account,time:Date=new Date()): Try[(Account, Account)] = {
    val fromTry=from.transfer(amount=amount,toAccountNr=to.number)
    val toTry=to.receiveTransfer(amount=amount,fromAccountNr=from.number)
    
    fromTry.flatMap(from => 
      toTry.map(to =>
      (from,to)
      )
    )
  }

  def newAccount(number: Long): Account = Account(number, Nil)
}

object InsufficientFunds {
  def apply() = Failure(new RuntimeException(message))
  val message="insufficient funds"
}
