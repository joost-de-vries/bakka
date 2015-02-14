import akka.actor.{ActorSystem, Status}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.reflect.ClassTag

class ActorSpec extends TestKit(ActorSpec.actorSystem())
with DefaultTimeout with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll{
  override protected def afterAll() = shutdown()

  def expectFailure[A <: Exception : ClassTag] = {
    expectMsgPF() {
      case Status.Failure(e: A) => true
    }
  }
}

object ActorSpec {
  def actorSystem() = ActorSystem("TestKitActorSystem",
    ConfigFactory.parseString(ActorSpec.config))

  // test specific configuration
  val config = """
    akka {
    loglevel = "WARNING"
    }
               """
}


