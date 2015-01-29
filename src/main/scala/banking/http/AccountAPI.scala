package banking.http

import akka.actor.ActorSystem
import akka.http.marshallers.sprayjson.SprayJsonSupport
import akka.http.marshalling._
import akka.http.server.Directives._
import akka.http.server._
import banking.actors.AccountActor.{WithdrawalRequest, DepositRequest, Balance, GetBalanceRequest}
import banking.actors.AccountManagerActor.{TransferRequest, Envelope}
import spray.json.DefaultJsonProtocol._
import akka.http.marshalling.{ToEntityMarshaller, ToResponseMarshallable}
import akka.pattern.ask
import akka.http.marshallers.xml.ScalaXmlSupport


trait AccountAPI {
  this: Main.type =>

  implicit def system: ActorSystem

  import system.dispatcher

  implicit val balanceFormat = jsonFormat1(Balance)
  implicit val marshaller: ToEntityMarshaller[Balance] = SprayJsonSupport.sprayJsonMarshaller[Balance]

  import ScalaXmlSupport._

  val routing: Route = {
    path("") {
      get {
        complete(index)
      }
    } ~
      pathPrefix("account" / LongNumber) { accountNr =>
        pathEnd {
          complete {
            (service ? Envelope(accountNumber = accountNr, GetBalanceRequest)).mapTo[Balance]
          }
        } ~
          pathPrefix("deposit" / LongNumber) { amount =>
            post {
              complete {
                (service ? Envelope(accountNumber = accountNr, DepositRequest(amount))).mapTo[Balance]
              }
            }
          } ~
          pathPrefix("withdraw" / LongNumber) { amount =>
            post {
              complete {
                (service ? Envelope(accountNumber = accountNr, WithdrawalRequest(amount))).mapTo[Balance]
              }
            }
          } ~
          pathPrefix("transfer" / LongNumber) { amount =>
            pathPrefix("to" / LongNumber) { toAccountNr =>
              post {
                complete {
                  (service ? Envelope(accountNumber = accountNr, TransferRequest(amount = amount, toAccountNumber = toAccountNr))).mapTo[Balance]
                }
              }
            }
          }
      }
  }
  lazy val index =
    <html>
      <body>
        <p>Defined resources:</p>
        <ul>
          <li>
            GET
            <a href="/account/123">account nr 123</a>
          </li>
          <li>
            POST
            <a href="/account/123/deposit/345">deposit 345 on account nr 123</a>
          </li>
        </ul>
      </body>
    </html>
}
