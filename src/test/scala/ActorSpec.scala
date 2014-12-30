import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by j.de.vries on 30-12-2014.
 */
class ActorSpec extends TestKit(ActorSystem("TestKitActorSystem",
  ConfigFactory.parseString(ActorSpec.config)))
with DefaultTimeout with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll{
  override def afterAll =shutdown()
}

object ActorSpec {
  // Define your test specific configuration here
  val config = """
    akka {
    loglevel = "WARNING"
    }
               """
}


