import java.io.{EOFException, IOException}

import scalaz.zio.{App, ZIO}
import Logger.logger
import scalaz.zio.console.Console

object MyApp extends App {

  type MyEnv[A] = Logger[A]

  override def run(args: List[String]): ZIO[Console, Nothing, Int] = {
    myAppLogic.provide {
      new Logger[Console] {
        override val logger: Logger.Service[Console] = Logger.StdOut.logger
      }
    }.fold(_ => 1, _ => 0)
  }

  def myAppLogic[A]: ZIO[MyEnv[A], IOException, Unit] = {
    for {
      _ <- logger().info("hello")
    } yield ()
  }

}
