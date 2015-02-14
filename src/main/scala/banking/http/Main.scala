package banking.http

import akka.actor.ActorSystem
import akka.http.Http
import akka.http.marshallers.sprayjson.SprayJsonSupport
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

  import banking.http.Main.system.dispatcher

  import scala.io.StdIn._

  val service = new AccountService(system.actorOf(AccountManagerActor.props, AccountManagerActor.name))

  implicit val materializer = ActorFlowMaterializer()
  implicit val timeout = Timeout(1000 millis)
  val serverBinding = Http().bind(interface = "localhost", port = 8080)

  val materializedMap = serverBinding startHandlingWith routing(service)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  readLine()
  serverBinding.unbind(materializedMap).onComplete(_ ⇒ system.shutdown())

}
