package banking

import java.util.Date

import scala.util.{Failure, Success, Try}

sealed trait Transaction{
  def time:Date
  def amount:Long
  
  def transact(account:Account,fromAmount:Long):Try[Long]
}

case class Deposit(time:Date,amount:Long,to:Account) extends Transaction{
  override def transact(account:Account,fromAmount: Long): Try[Long] = Success(fromAmount+amount)
}
case class Withdrawal(time:Date,amount:Long,from:Account) extends Transaction{
  override def transact(account:Account,fromAmount: Long): Try[Long] = if(fromAmount<amount) Success(fromAmount-amount) else Failure(new InsufficientFundsException())
  
}
case class Transfer(time:Date,amount:Long,from:Account,to:Account) extends Transaction{
  override def transact(account:Account,fromAmount: Long): Try[Long] = {
    if (from.number == account.number) {
      if (fromAmount < amount) Success(fromAmount - amount) else Failure(new InsufficientFundsException())

    } else if (to.number == account.number) Success(fromAmount + amount)
    else throw new IllegalStateException()
  }
}

case class Account(number:Long,history:List[Transaction]){
  def transfer(amount: Long, to: Account):Tuple2[Account,Account] = {
    val tx= Transfer(new Date(),amount, from=this,to=to)
    (this.copy(history=tx::this.history),to.copy(history=tx::to.history))
  }

  def withdraw(amount: Long) = this.copy(history=Withdrawal(new Date(),amount,from=this)::this.history)

  def balance= history.foldLeft(0L){(acc,tx)=> tx.transact(account=this,fromAmount=acc).get }
  def deposit(amount:Long)= this.copy(history=Deposit(new Date(), amount, to = this)::this.history)
}

object Account  {
  

  def New(number:Long):Account=Account(number,Nil)
}

class InsufficientFundsException() extends RuntimeException("insufficient funds")

