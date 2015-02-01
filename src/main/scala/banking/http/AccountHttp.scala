package banking.http

import akka.actor.ActorSystem
import akka.http.marshallers.sprayjson.SprayJsonSupport
import akka.http.marshallers.xml.ScalaXmlSupport._
import akka.http.marshalling.ToEntityMarshaller
import akka.http.server.Directives._
import akka.http.server._
import banking.actors.AccountActor.Balance
import spray.json.DefaultJsonProtocol._


object AccountHttp {

  def routing(accountService: AccountService)(implicit system: ActorSystem): Route = {
    import system.dispatcher
    implicit val balanceFormat = jsonFormat1(Balance)
    implicit val marshaller: ToEntityMarshaller[Balance] = SprayJsonSupport.sprayJsonMarshaller[Balance]
    
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
