package banking.http

import akka.actor.ActorSystem
import akka.http.Http
import akka.http.marshallers.sprayjson.SprayJsonSupport
import akka.http.marshallers.xml.ScalaXmlSupport
import akka.http.server.Directives
import akka.http.server.directives.AuthenticationDirectives._
import akka.pattern.ask
import akka.stream.FlowMaterializer
import akka.util.Timeout
import banking.actors.AccountActor.GetBalanceRequest
import banking.actors.AccountManagerActor
import banking.actors.AccountManagerActor.Envelope

import scala.concurrent.duration._


/**
 * Created by j.de.vries on 29-12-2014.
 */
object Main extends App with SprayJsonSupport {
  implicit val system = ActorSystem("webapi")

  import akka.http.marshallers.xml.ScalaXmlSupport._
  import akka.http.server.Directives._
  import banking.http.Main.system.dispatcher

import scala.io.StdIn._

  def auth =
    HttpBasicAuthenticator.provideUserName {
      case p@UserCredentials.Provided(name) ⇒ p.verifySecret(name + "-password")
      case _ ⇒ false
    }

  // create and start our service actor
  val service = system.actorOf(AccountManagerActor.props, AccountManagerActor.name)

  implicit val materializer = FlowMaterializer()
  implicit val timeout = Timeout(1000 millis)
  val serverBinding = Http().bind(interface = "localhost", port = 8080)

  val materializedMap = serverBinding startHandlingWith {
    get {
      path("") {
        complete(index)
      } ~
        pathPrefix("account" / LongNumber) { accountNr =>
          pathEnd {
            complete {
              (service ? Envelope(accountNumber = accountNr, GetBalanceRequest)).mapTo[Long].map(_.toString())
            }
//            path("deposit") {
//              post {
//                entity(as[Long]) {
//                  amount =>
//                    complete {
//                      (service ? Envelope(accountNumber = accountNr, DepositRequest(amount))).mapTo[Long].map(_.toString())
//                    }
//                }
//              }
//            }
          }
        }
    }
  }
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  readLine()
  serverBinding.unbind(materializedMap).onComplete(_ ⇒ system.shutdown())
  lazy val index =
    <html>
      <body>
        <p>Defined resources:</p>
        <ul>
          <li>
            <a href="/account/123">account nr 123</a>
          </li>
        </ul>
      </body>
    </html>
}
