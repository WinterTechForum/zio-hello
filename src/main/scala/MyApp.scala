import java.io.IOException

import scalaz.zio.{App, UIO, ZIO}
import scalaz.zio.console._

object MyApp extends App {

  type MyEnv = Console with Logger

  override def run(args: List[String]): ZIO[Console, Nothing, Int] = {
    myAppLogic.provideSome[Console] { thisConsole =>
      new Console with Logger {
        val console = thisConsole.console
        val logger = Logger.Dev.logger
      }
    }.fold(_ => 1, _ => 0)
  }

  sealed trait StrReadError
  case object EOF extends StrReadError
  case class IOError(ioException: IOException) extends StrReadError

  def getStrLnNonEmpty: ZIO[Console, StrReadError, String] = {
    getStrLn.refineOrDie {
      case e: IOException => IOError(e)
    } flatMap { s =>
      if (s == null || s.isEmpty)
        ZIO.fail(EOF)
      else
        ZIO.succeed(s)
    }
  }


  val myAppLogic: ZIO[MyEnv, StrReadError, Unit] = {
    val sayHello = for {
      _ <- putStrLn("Hello! What is your name?")
      n <- getStrLnNonEmpty
      _ <- ZIO.accessM[Logger](_.logger.info(s"$n was here"))
      _ <- putStrLn(s"Hello, $n, welcome to ZIO!")
    } yield ()

    sayHello.catchSome {
      case EOF =>
        for {
          _ <- putStrLn("You must specify a name")
          _ <- ZIO.accessM[Logger](_.logger.error("Name was not specified"))
          _ <- ZIO.fail(EOF)
        } yield ()
      case IOError(e) =>
        for {
          _ <- putStrLn(s"Error reading input: ${e.getMessage}")
          _ <- ZIO.accessM[Logger](_.logger.error(e))
          _ <- ZIO.fail(IOError(e))
        } yield ()
    }
  }

}
