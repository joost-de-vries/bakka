import banking.Account
import org.scalatest._

class TransferSpec extends FunSpec with GivenWhenThen {
  describe("An account") {

    it ("should allow depositing an amount") {
      Given("a new account")
      val newAccount = Account.New(1174L)

      When("a deposit is made")
      val amount=100L
      val resultAccount=newAccount.deposit(amount)

      Then("the history should contain one transaction")
      assertResult(1) {
        resultAccount.history.size
      }

      And("the balance should be equal to the deposited amount")

      assert(resultAccount.balance === amount)
    }
    
    it ("should allow withdrawing an amount") {
      Given("an account with one deposit")
      val deposit=100L
      val origAccount = Account.New(1174L).deposit(deposit)
      
      When("a withdrawal is made")
      val amount=70L
      val resultAccount=origAccount.withdraw(amount)

      Then("the history should contain two transactions")
      assertResult(2) {
        resultAccount.history.size
      }

      And("the balance should be equal to the original amount minus the withdrawal")

      assert(resultAccount.balance === deposit-amount)
    }
    
    it ("should allow transfering an amount") {
      Given("an account with one deposit")
      val deposit=100L
      val origAccount = Account.New(1174L).deposit(deposit)
      
      And("another new account")
      val account2 = Account.New(42L)
      
      When("a transfer is made")
      val amount=70L
      val (resultAccount,resultAccount2)=origAccount.transfer(amount,to=account2)

      Then("the tx should be part of the history of both accounts")
      assert( resultAccount.history.head.amount==amount)
      assert( resultAccount2.history.head.amount==amount)

      And("the sum of the accounts amounts should remain the same")

      assert(resultAccount.balance +
        resultAccount2.balance === 
        origAccount.balance+
          account2.balance)
    }
  }
}
