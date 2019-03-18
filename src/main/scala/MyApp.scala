import java.io.IOException

import scalaz.zio.{App, ZIO}
import scalaz.zio.console._

object MyApp extends App {

  def run(args: List[String]): ZIO[Console, Nothing, Int] = {
    myAppLogic.fold(_ => 1, _ => 0)
  }

  val myAppLogic: ZIO[Console, IOException, Unit] = for {
    _ <- putStrLn("Hello! What is your name?")
    n <- getStrLn
    _ <- putStrLn(s"Hello, ${n}, welcome to ZIO!")
  } yield ()

}
