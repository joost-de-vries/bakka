import java.util.Date

import banking.domain
import banking.domain._
import org.scalatest.{Matchers, FlatSpec, FunSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class GeneratorTransferSpec extends FunSpec with Matchers with GeneratorDrivenPropertyChecks{
  describe("accounts") {
    it("should not lose money") {

      //      //TODO write scalacheck generated test
      //      forAll { (accs: List[Account],txs:List[Transaction]) =>
//          forAll { tx: Transaction =>
//
//            true should be(true)
      //            whenever(n > 1) {
      //              n / 2 should be > 0
      //          }
//        }
//      }


    }
  }
}

object GenBanking {

  import org.scalacheck._
  import Gen._
  import Arbitrary._

  implicit val genAccounts = listOfN(20, Gen.resultOf(Account.newAccount _))

  implicit val genDeposit: Gen[Deposit] = Gen.resultOf(Deposit.apply _)

  implicit val genWithdrawal: Gen[Withdrawal] = Gen.resultOf(Withdrawal.apply _)

  def genTransfer(fromAccNr: Long, toAccNr: Long): Gen[Transfer] = for {
    time <- arbitrary[Date]
    amount <- arbitrary[Long]
  } yield new Transfer(TransferFrom(time, amount, toAccNr), TransferTo(time, amount, fromAccNr))
  
  
}