import org.scalatest.{FlatSpec, Matchers}
import scalaz.zio.{DefaultRuntime, Runtime}

class MyAppSpec extends FlatSpec with Matchers {

  val defaultRuntime = new DefaultRuntime {}

  val testableRuntime = new Runtime[Logger[Nothing]] {
    override val Environment = Logger.Nop
    override val Platform = defaultRuntime.withReportFailure(_ => Unit).withReportFatal(throw _).Platform
  }

  "MyApp" should "work" in {
    val exit = testableRuntime.unsafeRunSync(MyApp.myAppLogic)
    exit.succeeded should be (true)
  }

}
