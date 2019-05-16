import java.io.{EOFException, IOException}

import scalaz.zio.{App, ZIO}
import scalaz.zio.console._
import Logger.logger._

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

  def getStrLnNonEmpty: ZIO[Console, IOException, String] = {
    getStrLn.flatMap { s =>
      if (s.isEmpty)
        ZIO.fail(new EOFException("Input was empty"))
      else
        ZIO.succeed(s)
    }
  }


  val myAppLogic: ZIO[MyEnv, IOException, Unit] = {
    val sayHello = for {
      _ <- putStrLn("Hello! What is your name?")
      n <- getStrLnNonEmpty
      _ <- info(s"$n was here")
      _ <- putStrLn(s"Hello, $n, welcome to ZIO!")
    } yield ()

    sayHello.catchSome {
      case e: EOFException =>
        for {
          _ <- putStrLn("You must specify a name")
          _ <- error("Name was not specified")
          _ <- ZIO.fail(e)
        } yield ()
      case e: IOException =>
        for {
          _ <- putStrLn(s"Error reading input: ${e.getMessage}")
          _ <- error(e)
          _ <- ZIO.fail(e)
        } yield ()
    }
  }

}
