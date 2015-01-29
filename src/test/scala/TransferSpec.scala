import java.util.Date
import banking.domain.Account
import org.junit.runner.RunWith
import org.scalatest._
import Account._
import TestDate._
import org.scalatest.junit.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class TransferSpec extends FunSpec with Matchers with GivenWhenThen with TryValues {
  describe("An account") {

    it("should allow depositing an amount") {
      Given("a new account")
      val origAccount = newAccount(1174L)

      When("a deposit is made")
      val amount = 100L
      val resultAccountTry = origAccount.deposit(amount)

      Then("the result should be successful")
      resultAccountTry should be a 'success
      val resultAccount = resultAccountTry.get

      And("the history should contain one transaction")
      resultAccount.history.size should be(1)

      And("the balance should be equal to the deposited amount")
      resultAccount.balance should be(amount)
    }

    it("should determine a historical balance") {
      Given("a new account with a history")
      val origAccount = newAccount(1174L)
      val resultAccount = (for {
        acc1 <- origAccount.deposit(amount = 100L, time = JAN1_2001)
        acc2 <- acc1.withdraw(amount = 30L, time = JAN3_2001)
        acc3 <- acc2.withdraw(amount = 50L, time = JAN5_2001)
      } yield acc3).get

      When("a previous balance is requested")
      val balance2 = resultAccount.balance(date = JAN3_2001)

      Then("the result should be correct for that time")
      balance2 should be(70L)

      And("the balance should be correct for other points in time as well")
      val balance1 = resultAccount.balance(date = JAN1_2001)
      balance1 should be(100L)
      val balance0 = resultAccount.balance(date = JAN1_2000)
      balance0 should be(0L)
      val balance3 = resultAccount.balance(date = JAN5_2001)
      balance3 should be(20L)
      val balance4 = resultAccount.balance(date = JAN1_2002)
      balance4 should be(20L)
    }

    it("should allow withdrawing an amount") {
      Given("an account with sufficient funds")

      val origAccount = newAccount(1174L).deposit(100L).get

      When("a withdrawal is made")
      val amount = 70L
      val accountTry = origAccount.withdraw(amount)

      Then("the result should be successful")
      accountTry should be a 'success
      val resultAccount = accountTry.get

      And("the history should contain two transactions")
      resultAccount.history.size should be(2)

      And("the balance should be equal to the original amount minus the withdrawal")

      resultAccount.balance should be(origAccount.balance - amount)
    }

    it("should not allow withdrawing more than is available") {
      Given("an account with insufficient funds")

      val origAccount = newAccount(1174L).deposit(100L).get

      When("a withdrawal is made")
      val amount = 170L
      val resultAccountTry = origAccount.withdraw(amount)

      Then("the result should be unsuccessful")
      resultAccountTry.failure.exception should have message "insufficient funds"
    }

    it("should allow transferring an amount") {
      Given("an account with sufficient funds")
      val depositAmount = 100L
      val origAccount = newAccount(1174L).deposit(depositAmount).get

      And("another new account")
      val account2 = newAccount(42L)

      When("a transfer is made")
      val amount = 70L
      val resultTry = transfer(amount, from = origAccount, to = account2)

      Then("the transfer should be successful")
      resultTry should be a 'success
      val (resultAccount, resultAccount2) = resultTry.get

      And("the tx should be part of the history of both accounts")
      resultAccount.history.head.amount should be(amount)
      resultAccount.history.head.amount should be(amount)

      And("the sum of the accounts balances should remain the same")

      (resultAccount.balance + resultAccount2.balance) should be(
        origAccount.balance + account2.balance)
    }

    it("should not allow transferring more than is available") {
      Given("an account with insufficient funds")
      val depositAmount = 100L
      val origAccount = newAccount(1174L).deposit(depositAmount).get

      And("another new account")
      val account2 = newAccount(42L)

      When("a transfer is made")
      val amount = 170L
      val resultTry = transfer(amount, from = origAccount, to = account2)

      Then("the transfer should be unsuccessful")
      resultTry.failure.exception should have message "insufficient funds"
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

