import akka.testkit.TestProbe
import banking.actors.AccountActor.{GetBalanceRequest, TransferFromRequest, DepositRequest}
import banking.actors.AccountManagerActor
import scala.concurrent.duration._


/**
 * Created by j.de.vries on 30-12-2014.
 */
class AccountManagerSpec extends ActorSpec{
  import AccountManagerActor._

  def accountManagerRef = system.actorOf(AccountManagerActor.props)

  "An AccountManagerActor" should {
    "Create a new accountactor and forward the depositrequest and balance response" in {
      within(500 millis) {
        accountManagerRef ! Envelope(accountNumber = 100L, DepositRequest(50L))
        expectMsg(50L)
      }
    }
    "Get the same accountactor and forward the depositrequest and balance response" in {
      within(500 millis) {
        val accountManager = accountManagerRef
        accountManager ! Envelope(accountNumber = 100L, DepositRequest(50L))
        expectMsg(50L)
        accountManager ! Envelope(accountNumber = 100L, DepositRequest(50L))
        expectMsg(100L)
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
        expectMsg(50L)
        accountManager ! Envelope(accountNumber = 100L, TransferRequest(amount = 20L, toAccountNumber = 123L))
        expectMsg(30L)
        accountManager ! Envelope(accountNumber = 123L, GetBalanceRequest)
        expectMsg(20L)
        accountManager ! Envelope(accountNumber = 100L, GetBalanceRequest)
        expectMsg(30L)
      }
    }
  }
}
