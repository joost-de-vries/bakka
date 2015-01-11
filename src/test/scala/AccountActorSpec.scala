import java.util.Date

import banking.actors.AccountActor
import banking.domain.{Deposit, Account, InsufficientFunds}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.language.postfixOps
import scala.util.Random

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit._
import scala.concurrent.duration._
import scala.collection.immutable

class AccountActorSpec
  extends ActorSpec {

  import AccountActor._

  def accountRef(initialAmount: Long) = system.actorOf(AccountActor.props(Account(number = 100L, history = List(Deposit(new Date(), initialAmount)))))

  "An AccountActor" should {
    "Respond to a deposit with the new balance" in {
      within(500 millis) {
        accountRef(0L) ! DepositRequest(50L)
        expectMsg(50L)
      }
    }
    "Not allow a withdrawal in case of insufficient funds" in {
      within(500 millis) {
        accountRef(50L) ! WithdrawalRequest(70L)
        expectMsg(InsufficientFunds.message)
      }
    }
    "Respond to a withdrawal with the new balance" in {
      within(500 millis) {
        accountRef(50L) ! WithdrawalRequest(40L)
        expectMsg(10L)
      }
    }
    "Respond to a balance request with the balance" in {
      within(500 millis) {
        accountRef(10L) ! GetBalanceRequest
        expectMsg(10L)
      }
    }
    "Respond to a transfer from with the balance" in {
      within(500 millis) {
        val toAccount = TestProbe()
        val account = accountRef(10L)
        account ! TransferFromRequest(amount = 7L, toAccountNumber = 200L, toAccount.ref)
        account ! 1174L
        expectMsg(3L)
        toAccount.expectMsg(500 millis, TransferToRequest(amount = 7L, fromAccountNumber = 100L))
      }
    }
    "Not allow a transfer in case of insufficient funds" in {
      within(1000 millis) {
        val toAccount = TestProbe()
        val account = accountRef(5L)
        account ! TransferFromRequest(amount = 7L, toAccountNumber = 200L, toAccount.ref)
        expectMsg(InsufficientFunds.message)
        toAccount.expectNoMsg(500 millis)
      }
    }
    "Respond to a transfer to with the balance" in {
      within(500 millis) {
        accountRef(3) ! TransferToRequest(amount = 7L, fromAccountNumber = 200L)
        expectMsg(10L)
      }
    }
  }
}
