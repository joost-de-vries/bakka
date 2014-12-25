import banking.{Transaction, Account}
import org.scalatest.{Matchers, FlatSpec, FunSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

/**
 * Created by j.de.vries on 25-12-2014.
 */
class GeneratorTransferSpec extends FunSpec with Matchers with GeneratorDrivenPropertyChecks{
  describe("accounts") {
    it("should support invariants") {
      //TODO write scalacheck generated test
//      forAll { (acc1: Account,acc2:Account,txs:List[Transaction]) =>
//          forAll { tx: Transaction =>
//
//            true should be(true)
////            whenever(n > 1) {
////              n / 2 should be > 0
////          }
//        }
//      }


    }
  }
}

object GenBanking {

  import org.scalacheck._
  import Gen._
  import Arbitrary._

  val genAccounts = listOfN(20,Gen.resultOf(Account.newAccount _))
  
//  def genDeposit(account:Account) = for {
//    acc <- Gen.resultOf(Account.deposit() _)
//
//  }
//
//  def genTx: Gen[Transaction] = oneOf(genDeposit, genWithdrawal, genTransfer)

}