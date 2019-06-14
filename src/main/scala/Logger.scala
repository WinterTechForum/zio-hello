import scalaz.zio.{ZIO, console}
import scalaz.zio.console.Console

trait Logger[A] extends Serializable {
  val logger: Logger.Service[A]
}

object Logger extends Serializable {
  trait Service[A] {
    def info(s: String): ZIO[A, Nothing, Unit]
  }

  object StdOut extends Logger[Console] {
    override val logger: Service[Console] = new Service[Console] {
      override def info(s : String): ZIO[Console, Nothing, Unit] = {
        console.putStrLn(s"[info] $s")
      }
    }
  }

  object Nop extends Logger[Nothing] {
    override val logger: Service[Nothing] = new Service[Nothing] {
      override def info(s : String): ZIO[Nothing, Nothing, Unit] = ZIO.unit
    }
  }

  case class logger[A]() extends Logger.Service[Logger[A]] {
    override def info(s: String): ZIO[Logger[A], Nothing, Unit] = ZIO.accessM[Logger[A]](_.logger.info(s))
  }

}
