import banking.actors.AccountActor
import banking.domain.InsufficientFunds
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.Random

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.{ TestActors, DefaultTimeout, ImplicitSender, TestKit }
import scala.concurrent.duration._
import scala.collection.immutable

class AccountActorSpec
  extends ActorSpec {

  import AccountActor._
  val accountRef = system.actorOf(AccountActor.props(100L))

  "An AccountActor" should {
    "Respond to a deposit with the new balance" in {
      within(500 millis) {
        accountRef ! DepositRequest(50L)
        expectMsg(50L)
      }
    }
    "Not allow a withdrawal in case of insufficient funds" in {
      within(500 millis) {
        accountRef ! WithdrawalRequest(70L)
        expectMsg(InsufficientFunds.message)
      }
    }
    "Respond to a withdrawal with the new balance" in {
      within(500 millis) {
        accountRef ! WithdrawalRequest(40L)
        expectMsg(10L)
      }
    }
    "Respond to a balance request with the balance" in {
      within(500 millis) {
        accountRef ! GetBalanceRequest
        expectMsg(10L)
      }
    }
    "Respond to a transfer from with the balance" in {
      within(500 millis) {
        accountRef ! TransferFromRequest(amount=7L,toAccountNumber = 200L)
        expectMsg(3L)
      }
    }
    "Respond to a transfer to with the balance" in {
      within(500 millis) {
        accountRef ! TransferToRequest(amount=7L,fromAccountNumber = 200L)
        expectMsg(10L)
      }
    }
  }
}
