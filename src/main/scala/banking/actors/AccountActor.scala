package banking.actors


import java.util.UUID

import akka.actor._
import akka.actor.ActorLogging
import banking.actors.AccountManagerActor.DoNotUnderstand
import banking.domain.Account

import scala.util.{Try, Failure, Success}

object AccountActor {
  def props(number:Long) = Props(new AccountActor(number))
  def name = "simple-account-actor"
  
  case class DepositRequest(amount:Long)
  case class WithdrawalRequest(amount:Long)
  case object GetBalanceRequest
  case class TransferToRequest(amount:Long,fromAccountNumber:Long)
  case class TransferFromRequest(amount:Long,toAccountNumber:Long)
  case object AccountRequest
}

class AccountActor(number:Long) extends Actor with ActorLogging  {
  import AccountActor._
  var account = Account.newAccount(number)
  

  override def receive: Receive = {
    case DepositRequest(amount) => {
        account = account.deposit(amount).get
        sender() ! account.balance
      }
    case WithdrawalRequest(amount)=>{
      account.withdraw(amount) match {
        case Success(newAccount) => 
          account=newAccount
          sender() ! account.balance
        case Failure(e) => sender() ! e.getMessage()
      }      
    }
    case TransferFromRequest(amount,toAccNumber) => {
      update(account.transfer(amount,toAccountNr = toAccNumber) )
    }
    case TransferToRequest(amount,fromAccNumber) => {
      update(account.receiveTransfer(amount,fromAccountNr = fromAccNumber) )
    }
    case GetBalanceRequest => {
      sender() ! account.balance
    }
    case _ => sender() ! DoNotUnderstand.message
  }
   
  /** update account or send error message */
  private def update(accTry:Try[Account])={
    accTry match {
      case Success(newAccount) =>
        account=newAccount
        sender() ! account.balance
      case Failure(e) => sender() ! e.getMessage()
    }
    
  }
}