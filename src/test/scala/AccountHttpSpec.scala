import akka.http.model.StatusCodes._
import akka.http.model.{ContentTypes, StatusCodes}
import akka.http.server._
import akka.http.testkit.ScalatestRouteTest
import banking.actors.AccountActor.Balance
import banking.actors.AccountManagerActor
import banking.http.{AccountHttp, AccountService, HasActorSystem}
import org.scalatest.{Matchers, WordSpec}

import scala.language.postfixOps

class AccountHttpSpec extends WordSpec with HasActorSystem with AccountHttp with Matchers with ScalatestRouteTest {

  val service = new AccountService(system.actorOf(AccountManagerActor.props, AccountManagerActor.name))

  def theRoute: Route = routing(service)

  "The Account http api" should {

    "respond with the balance to a GetBalanceRequest" in {
      import akka.http.marshallers.sprayjson.SprayJsonSupport._
      import banking.http.AccountJsonProtocol.balanceFormat


      Get("/account/234") ~> theRoute ~> check {
        status shouldEqual OK
        responseEntity.contentType() shouldEqual ContentTypes.`application/json`

        responseAs[Balance] shouldBe Balance(0)
      }
    }
  }
}
