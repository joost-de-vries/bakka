import banking.actors.AccountActor.{GetBalanceRequest, TransferFromRequest, DepositRequest}
import banking.actors.AccountManagerActor
import scala.concurrent.duration._


/**
 * Created by j.de.vries on 30-12-2014.
 */
class AccountManagerSpec extends ActorSpec{
  import AccountManagerActor._
  val accountManagerRef = system.actorOf(AccountManagerActor.props)

  "An AccountManagerActor" should {
    "Create a new accountactor and forward the depositrequest and balance response" in {
      within(500 millis) {
        accountManagerRef ! Envelope(accountNumber = 100L, DepositRequest(50L))
        expectMsg(50L)
      }
    }
    "Get the same accountactor and forward the depositrequest and balance response" in {
      within(500 millis) {
        accountManagerRef ! Envelope(accountNumber = 100L, DepositRequest(50L))
        expectMsg(100L)
        accountManagerRef ! Envelope(accountNumber = 100L, DepositRequest(50L))
        expectMsg(150L)
      }
    }
    "Not forward transferfrom or transferto request" in {
      within(500 millis) {
        accountManagerRef ! Envelope(accountNumber = 100L, TransferFromRequest(amount=50L,toAccountNumber = 123L))
        expectMsg(NotAllowed.message)
      }
    }
    "Respond to a transfer request and increase the counter account" in {
      within(1500 millis) {
        accountManagerRef ! Envelope(accountNumber = 100L,TransferRequest(amount=20L,toAccountNumber = 123L))
        expectMsg(130L)
        accountManagerRef ! Envelope(accountNumber = 123L,GetBalanceRequest)
        expectMsg(20L)
      }
    }
  }
}
