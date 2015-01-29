package banking.http

import akka.actor.ActorSystem
import akka.http.Http
import akka.http.marshallers.sprayjson.SprayJsonSupport
import akka.http.marshallers.xml.ScalaXmlSupport
import akka.http.server.{Route, Directives}
import akka.http.server.directives.AuthenticationDirectives._
import akka.http.unmarshalling.FromEntityUnmarshaller
import akka.stream.FlowMaterializer
import akka.util.Timeout
import banking.actors.AccountActor.{WithdrawalRequest, Balance, DepositRequest, GetBalanceRequest}
import banking.actors.AccountManagerActor
import banking.actors.AccountManagerActor.{TransferRequest, Envelope}
import spray.json._
import DefaultJsonProtocol._
import scala.language.postfixOps

import scala.concurrent.duration._


object Main extends App with SprayJsonSupport with AccountAPI {
  implicit lazy val system = ActorSystem("webapi")

  import banking.http.Main.system.dispatcher

  import scala.io.StdIn._

  val service = system.actorOf(AccountManagerActor.props, AccountManagerActor.name)

  implicit val materializer = FlowMaterializer()
  implicit val timeout = Timeout(1000 millis)
  val serverBinding = Http().bind(interface = "localhost", port = 8080)

  val materializedMap = serverBinding startHandlingWith routing
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  readLine()
  serverBinding.unbind(materializedMap).onComplete(_ ⇒ system.shutdown())

}
