import java.util.Date

import akka.testkit._
import banking.actors.AccountActor
import banking.domain.{Account, Deposit, InsufficientFundsException}

import scala.concurrent.duration._
import scala.language.postfixOps

class AccountActorSpec
  extends ActorSpec {

  import banking.actors.AccountActor._

  def accountRef(initialAmount: Long) = system.actorOf(AccountActor.props( 
    Account(number = 100L, history = List(Deposit(new Date(), initialAmount))))
  )

  "An AccountActor" should {
    "Respond to a deposit with the new balance" in {
      within(500 millis) {
        accountRef(0L) ! DepositRequest(50L)
        expectMsg(Balance(50L))
        expectNoMsg()
      }
    }
    "Not allow a withdrawal in case of insufficient funds" in {
      within(500 millis) {
        accountRef(50L) ! WithdrawalRequest(70L)
        expectFailure[InsufficientFundsException]
        expectNoMsg()
      }
    }
    "Respond to a withdrawal with the new balance" in {
      within(500 millis) {
        accountRef(50L) ! WithdrawalRequest(40L)
        expectMsg(Balance(10L))
        expectNoMsg()
      }
    }
    "Respond to a balance request with the balance" in {
      within(500 millis) {
        accountRef(10L) ! GetBalanceRequest
        expectMsg(Balance(10L))
        expectNoMsg()
      }
    }
    "Respond to a transferFrom with the balance" in {
      within(500 millis) {
        val toAccount = TestProbe()
        val account = accountRef(10L)
        account ! TransferFromRequest(amount = 7L, toAccountNumber = 200L, toAccount.ref)
        account ! Balance(1174L)
        expectMsg(Balance(3L))
        toAccount.expectMsg(500 millis, TransferToRequest(amount = 7L, fromAccountNumber = 100L))
        expectNoMsg()
      }
    }
    "Not allow a transfer in case of insufficient funds" in {
      within(1500 millis) {
        val toAccount = TestProbe()
        val account = accountRef(5L)
        account ! TransferFromRequest(amount = 7L, toAccountNumber = 200L, toAccount.ref)
        expectFailure[InsufficientFundsException]
        toAccount.expectNoMsg(500 millis)
        expectNoMsg()
      }
    }
    "Respond to a transferTo with the balance" in {
      within(500 millis) {
        accountRef(3) ! TransferToRequest(amount = 7L, fromAccountNumber = 200L)
        expectMsg(Balance(10L))
        expectNoMsg()
      }
    }
    "Handle no response from counter account when transferring" in {
      within(AccountActor.TRANSFER_TIMEOUT + 500 millis) {
        val toAccount = TestProbe()
        val initialAmount = 50L
        val account = accountRef(initialAmount)
        account ! TransferFromRequest(amount = 40L, toAccountNumber = 200L, toAccount.ref)

        toAccount.expectMsg(TransferToRequest(amount = 40L, fromAccountNumber = 100L))
        //we're intentionally not confirming the transfer. I.e. no toAccount.reply(40L)
        account ! GetBalanceRequest //send another message just to be confusing
        
        expectMsgPF(AccountActor.TRANSFER_TIMEOUT + 100 millis) {
          case string: String => string.contains("timeout") should be(true)
        }

        //our balancerequest should not resolve the transfer timeout and should be handled after the timeout
        expectMsg(Balance(initialAmount))
        expectNoMsg()

      }
    }
  }
}
