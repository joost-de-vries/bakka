import akka.testkit.TestProbe
import banking.actors.AccountActor.{Balance, DepositRequest, GetBalanceRequest, TransferFromRequest}
import banking.actors.AccountManagerActor

import scala.concurrent.duration._
import scala.language.postfixOps


class AccountManagerSpec extends ActorSpec{

  import banking.actors.AccountManagerActor._

  def accountManagerRef = system.actorOf(AccountManagerActor.props)

  "An AccountManagerActor" should {
    "Create a new accountactor and forward the depositrequest and balance response" in {
      within(500 millis) {
        accountManagerRef ! Envelope(accountNumber = 100L, DepositRequest(50L))
        expectMsg(Balance(50L))
      }
    }
    "Get the same accountactor and forward the depositrequest and balance response" in {
      within(500 millis) {
        val accountManager = accountManagerRef
        accountManager ! Envelope(accountNumber = 100L, DepositRequest(50L))
        expectMsg(Balance(50L))
        accountManager ! Envelope(accountNumber = 100L, DepositRequest(50L))
        expectMsg(Balance(100L))
      }
    }
    "Not forward transferfrom or transferto request" in {
      within(500 millis) {
        accountManagerRef ! Envelope(accountNumber = 100L, TransferFromRequest(amount = 50L, toAccountNumber = 123L, TestProbe().ref))
        expectMsg(NotAllowed.message)
      }
    }
    "Respond to a transfer request and increase the counter account" in {
      within(500 millis) {
        val accountManager = accountManagerRef
        accountManager ! Envelope(accountNumber = 100L, DepositRequest(50L))
        expectMsg(Balance(50L))
        accountManager ! Envelope(accountNumber = 100L, TransferRequest(amount = 20L, toAccountNumber = 123L))
        expectMsg(Balance(30L))
        accountManager ! Envelope(accountNumber = 123L, GetBalanceRequest)
        expectMsg(Balance(20L))
        accountManager ! Envelope(accountNumber = 100L, GetBalanceRequest)
        expectMsg(Balance(30L))
      }
    }
    "Not lose actions when transferring" in {
      within(500 millis) {
        val accountMgr = accountManagerRef
        val accNr = 3
        val counterAccNr = 7

        for (i <- 0 until 10) {
          accountMgr ! Envelope(accNr, DepositRequest(100))
          accountMgr ! Envelope(accNr, TransferRequest(70, toAccountNumber = counterAccNr))

        }
        for (msg <- receiveN(20)) {
          msg.isInstanceOf[Balance] should be(true)
        }

        accountMgr ! Envelope(accNr, GetBalanceRequest)
        expectMsg(Balance(300L))
        accountMgr ! Envelope(counterAccNr, GetBalanceRequest)
        expectMsg(Balance(700L))
      }
    }

  }
}
