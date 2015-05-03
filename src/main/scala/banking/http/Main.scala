package banking.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import banking.actors.AccountManagerActor

import scala.concurrent.duration._
import scala.language.postfixOps

trait HasActorSystem {
  implicit def system: ActorSystem
}

object Main extends App with AccountHttp with HasActorSystem with SprayJsonSupport {
  implicit lazy val system = ActorSystem("webapi")
  implicit val executor = system.dispatcher

  implicit def materializer = ActorFlowMaterializer()

  import scala.io.StdIn._

  val service = new AccountService(system.actorOf(AccountManagerActor.props, AccountManagerActor.name))

  implicit val timeout = Timeout(1000 millis)
  val serverBinding = Http().bindAndHandle(interface = "localhost", port = 8080, handler = routing(service))

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  readLine()
  system.shutdown()

}
