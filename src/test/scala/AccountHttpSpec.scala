import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
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
      import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
      import banking.http.AccountJsonProtocol.balanceFormat


      Get("/account/234") ~> theRoute ~> check {
        status shouldEqual OK
        responseEntity.contentType() shouldEqual ContentTypes.`application/json`

        responseAs[Balance] shouldBe Balance(0)
      }
    }
  }
}
