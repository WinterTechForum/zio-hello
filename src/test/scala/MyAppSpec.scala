import java.io.IOException
import java.util

import org.scalatest.{FlatSpec, Matchers}
import scalaz.zio.{Exit, Runtime, UIO, ZIO}
import scalaz.zio.console.Console
import scalaz.zio.internal.{Executor, Platform, PlatformLive}

class MyAppSpec extends FlatSpec with Matchers {

  def testableRuntime(inputs: List[String]) = {
    new Runtime[TestableConsole with Logger] {
      override val Environment = new TestableConsole with NopLogger {
        override def in = inputs
      }

      val basePlatform = PlatformLive.Default

      override val Platform = new Platform {
        override def executor: Executor = basePlatform.executor

        override def nonFatal(t: Throwable): Boolean = basePlatform.nonFatal(t)

        override def reportFailure(cause: Exit.Cause[_]): Unit = ()

        override def newWeakHashMap[A, B](): util.Map[A, B] = basePlatform.newWeakHashMap()
      }
    }
  }

  "MyApp" should "work" in {
    val runtime = testableRuntime(List("asdf"))
    val exit = runtime.unsafeRunSync(MyApp.myAppLogic)
    exit.succeeded should be (true)
    val out = runtime.Environment.out.result()
    out shouldEqual List(
      "Hello! What is your name?\n",
      "Hello, asdf, welcome to ZIO!\n"
    )
  }

  "MyApp" should "fail with no input" in {
    val runtime = testableRuntime(List())
    val exit = runtime.unsafeRunSync(MyApp.myAppLogic)
    exit.succeeded should be (false)
    val out = runtime.Environment.out.result()
    out shouldEqual List(
      "Hello! What is your name?\n",
      "Error reading input: no more inputs\n"
    )
  }

  "MyApp" should "fail with an empty input" in {
    val runtime = testableRuntime(List(""))
    val exit = runtime.unsafeRunSync(MyApp.myAppLogic)
    exit.succeeded should be (false)
    val out = runtime.Environment.out.result()
    out shouldEqual List(
      "Hello! What is your name?\n",
      "You must specify a name\n"
    )
  }

}

trait TestableConsole extends Console {
  val out = List.newBuilder[String]
  def in: List[String]

  val console: Console.Service[Any] = new Console.Service[Any] {
    val inIterator = in.iterator

    final def putStr(line: String): UIO[Unit] = UIO {
      out += line
    }

    final def putStrLn(line: String): UIO[Unit] = UIO {
      out += line + "\n"
    }

    final val getStrLn: ZIO[Any, IOException, String] = {
      if (inIterator.hasNext) {
        ZIO.succeed(inIterator.next())
      }
      else {
        ZIO.fail(new IOException("no more inputs"))
      }
    }
  }
}

trait NopLogger extends Logger {
  override val logger = new Logger.Service[Any] {
    override def info(s: String) = ZIO.unit

    override def error(s: String) = ZIO.unit

    override def error(e: Throwable) = ZIO.unit
  }
}
