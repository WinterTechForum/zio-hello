import scalaz.zio.ZIO

trait Logger extends Serializable {
  val logger: Logger.Service[Any]
}

object Logger extends Serializable {
  trait Service[R] {
    def info(s: String): ZIO[Logger, Nothing, Unit]
    def error(s: String): ZIO[Logger, Nothing, Unit]
    def error(e: Throwable): ZIO[Logger, Nothing, Unit]
  }

  object Dev extends Logger {
    override val logger: Service[Any] = new Service[Any] {
      override def info(s : String): ZIO[Logger, Nothing, Unit] = {
        ZIO.effectTotal {
          println(s"[info] $s")
        }
      }

      override def error(s: String): ZIO[Logger, Nothing, Unit] = {
        ZIO.effectTotal {
          println(s"[error] $s")
        }
      }

      override def error(e: Throwable): ZIO[Logger, Nothing, Unit] = {
        ZIO.effectTotal {
          println(s"[error] ${e.getMessage}")
          e.printStackTrace()
        }
      }
    }
  }

  object logger extends Logger.Service[Logger] {
    override def info(s: String): ZIO[Logger, Nothing, Unit] = ZIO.accessM[Logger](_.logger.info(s))

    override def error(s: String): ZIO[Logger, Nothing, Unit] = ZIO.accessM[Logger](_.logger.error(s))

    override def error(e: Throwable): ZIO[Logger, Nothing, Unit] = ZIO.accessM[Logger](_.logger.error(e))
  }

}
