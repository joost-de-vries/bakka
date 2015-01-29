import java.util.Date

import akka.testkit._
import banking.actors.{PersistentAccountActor, AccountActor}
import banking.domain.{Account, Deposit, InsufficientFunds}

import scala.concurrent.duration._
import scala.language.postfixOps

class PersistentAccountActorSpec
  extends ActorSpec {

  import banking.actors.AccountActor._

  def accountRef(initialAmount: Long) = system.actorOf(PersistentAccountActor.props(
    Account(number = 100L, history = List(Deposit(new Date(), initialAmount))))
  )

  "A PersistentAccountActor" should {
    "Respond to a deposit with the new balance" in {
      within(500 millis) {
        accountRef(0L) ! DepositRequest(50L)
        expectMsg(Balance(50L))
        expectNoMsg()
      }
    }
  }
}
