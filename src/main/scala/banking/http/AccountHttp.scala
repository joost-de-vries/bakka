package banking.http

import akka.actor.ActorSystem
import akka.http.marshallers.sprayjson.SprayJsonSupport
import akka.http.marshallers.xml.ScalaXmlSupport._
import akka.http.marshalling.ToEntityMarshaller
import akka.http.model.{HttpResponse, StatusCodes}
import akka.http.server.Directives._
import akka.http.server._
import banking.actors.AccountActor
import banking.actors.AccountActor.Balance
import banking.domain.InsufficientFundsException
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


trait AccountHttp {
  this: HasActorSystem =>

  def routing(accountService: AccountService)(implicit system: ActorSystem): Route = {
    import banking.http.AccountJsonProtocol._
    import system.dispatcher

    path("") {
      get {
        complete(index)
      }
    } ~
      pathPrefix("account" / LongNumber) { accountNr =>
        pathEnd {
          complete {
            accountService.balance(accountNr)
          }
        } ~
          handleExceptions(insufficientFundsHandler) {
            pathPrefix("deposit" / LongNumber) { amount =>
              post {
                complete {
                  accountService.deposit(accountNumber = accountNr, amount = amount)
                }
              }
            } ~
              pathPrefix("withdraw" / LongNumber) { amount =>
                post {
                  complete {
                    accountService.withdraw(accountNumber = accountNr, amount = amount)
                  }
                }
              } ~
              pathPrefix("transfer" / LongNumber) { amount =>
                pathPrefix("to" / LongNumber) { toAccountNr =>
                  post {
                    complete {
                      accountService.transfer(fromAccountNumber = accountNr, amount = amount, toAccountNumber = toAccountNr)
                    }
                  }
                }
              }
          }
      }
  }

  private def insufficientFundsHandler = ExceptionHandler {
    case e: InsufficientFundsException =>
      println("caught exception")
      extractUri { uri =>
        complete(HttpResponse(StatusCodes.BadRequest, entity = e.getMessage))
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

object AccountJsonProtocol extends DefaultJsonProtocol {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val balanceFormat: RootJsonFormat[AccountActor.Balance] = jsonFormat1(Balance)
  implicit val balanceMarshaller: ToEntityMarshaller[Balance] = SprayJsonSupport.sprayJsonMarshaller[Balance]
}
