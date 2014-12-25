import java.util.Date

import banking.Account
import org.scalatest._
import banking.Account._
import TestDate._

import scala.util.Try

class TransferSpec extends FunSpec with GivenWhenThen {
  describe("An account") {

    it("should allow depositing an amount") {
      Given("a new account")
      val origAccount = newAccount(1174L)

      When("a deposit is made")
      val amount = 100L
      val resultAccountTry = deposit(to = origAccount, amount = amount)

      Then("the result should be successful")
      val resultAccount = resultAccountTry.get

      And("the history should contain one transaction")
      assertResult(1) {
        resultAccount.history.size
      }

      And("the balance should be equal to the deposited amount")

      assert(resultAccount.balance === amount)
    }

    it("should determine a historical balance") {
      Given("a new account with a history")
      val origAccount = newAccount(1174L)
      val resultAccount = (for {acc1 <- deposit(to = origAccount, amount = 20L, time = JAN1_2001)
                               acc2 <- deposit(to = acc1, amount = 30L, time = JAN3_2001)
                               acc3 <- deposit(to = acc2, amount = 50L, time = JAN5_2001)} yield acc3).get

      When("a previous balance is requested")
      val balance2 = resultAccount.balance(date = JAN3_2001)

      Then("the result should be correct for that time")
      assert(balance2 === 50L)
      
      And("the balance should be correct for other points in time as well")
      val balance1=resultAccount.balance(date=JAN1_2001)
      assert(balance1===20L)
      val balance0=resultAccount.balance(date=JAN1_2000)
      assert(balance0===0L)
      val balance3= resultAccount.balance(date=JAN5_2001)
      assert(balance3===100L)
      val balance4=resultAccount.balance(date=JAN1_2002)
      assert(balance4===100L)

      

    }

    it("should allow withdrawing an amount") {
      Given("an account with sufficient funds")

      val origAccount = deposit(newAccount(1174L), 100L).get

      When("a withdrawal is made")
      val amount = 70L
      val resultAccountTry = withdraw(origAccount, amount)

      Then("the result should be successful")
      val resultAccount = resultAccountTry.get

      And("the history should contain two transactions")
      assertResult(2) {
        resultAccount.history.size
      }

      And("the balance should be equal to the original amount minus the withdrawal")

      assert(resultAccount.balance === origAccount.balance - amount)
    }

    it("should not allow withdrawing more than is available") {
      Given("an account with insufficient funds")

      val origAccount = deposit(newAccount(1174L), 100L).get

      When("a withdrawal is made")
      val amount = 170L
      val resultAccountTry = withdraw(origAccount, amount)

      Then("the result should be unsuccessful")
      assert(resultAccountTry.isFailure)
    }

    it("should allow transferring an amount") {
      Given("an account with sufficient funds")
      val depositAmount = 100L
      val origAccount = deposit(newAccount(1174L), depositAmount).get

      And("another new account")
      val account2 = newAccount(42L)

      When("a transfer is made")
      val amount = 70L
      val resultTry = transfer(amount, from = origAccount, to = account2)

      Then("the transfer should be successful")
      val (resultAccount, resultAccount2) = resultTry.get

      And("the tx should be part of the history of both accounts")
      assert(resultAccount.history.head.amount == amount)
      assert(resultAccount.history.head.amount == amount)

      And("the sum of the accounts balances should remain the same")

      assert(resultAccount.balance + resultAccount2.balance ===
        origAccount.balance + account2.balance)
    }

    it("should not allow transferring more than is available") {
      Given("an account with insufficient funds")
      val depositAmount = 100L
      val origAccount = deposit(newAccount(1174L), depositAmount).get

      And("another new account")
      val account2 = newAccount(42L)

      When("a transfer is made")
      val amount = 170L
      val resultTry = transfer(amount, from = origAccount, to = account2)

      Then("the transfer should be unsuccessful")
      assert(resultTry.isFailure)

    }
  }
}

object TestDate {
  val JAN1_2000: Date = new Date(946684800000L)
  val JAN1_2001: Date = new Date(978307200000L)
  val JAN3_2001: Date = new Date(978480000000L)
  val JAN5_2001: Date = new Date(978652800000L)
  val JAN8_2001: Date = new Date(978912000000L)
  val MAR1_2001: Date = new Date(983404800000L)
  val MAY5_2001: Date = new Date(988675200000L)
  val NOV11_2001: Date = new Date(1004572800000L)
  val JAN1_2002: Date = new Date(1009843200000L)
  val MAY5_2002: Date = new Date(1020556800000L)
  val JAN1_2003: Date = new Date(1041379200000L)
  val APR5_2004: Date = new Date(1081123200000L)
}

