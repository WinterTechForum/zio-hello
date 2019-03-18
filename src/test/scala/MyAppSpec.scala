import java.io.IOException
import java.util

import org.scalatest.{FlatSpec, Matchers}
import scalaz.zio.{Exit, Runtime, UIO, ZIO}
import scalaz.zio.console.Console
import scalaz.zio.console.Console.Service
import scalaz.zio.internal.{Executor, Platform, PlatformLive}

class MyAppSpec extends FlatSpec with Matchers {

  def testableRuntime(inputs: List[String]) = {
    new Runtime[TestableConsole] {
      override val Environment: TestableConsole = new TestableConsole {
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
    out should contain ("Hello! What is your name?\n")
    out should contain ("Hello, asdf, welcome to ZIO!\n")
  }

  "MyApp" should "fail with no input" in {
    val runtime = testableRuntime(List())
    val exit = runtime.unsafeRunSync(MyApp.myAppLogic)
    exit.succeeded should be (false)
    val out = runtime.Environment.out.result()
    out should contain only "Hello! What is your name?\n"
  }

}

trait TestableConsole extends Console {
  val out = List.newBuilder[String]
  def in: List[String]

  val console: Service[Any] = new Service[Any] {
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
