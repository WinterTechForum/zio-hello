import scalaz.zio.{UIO, ZIO}

trait Logger extends Serializable {
  val logger: Logger.Service[Any]
}

object Logger extends Serializable {
  trait Service[R] {
    def info(s: String): UIO[Unit]
    def error(s: String): UIO[Unit]
    def error(e: Throwable): UIO[Unit]
  }

  object Dev extends Logger {
    override val logger: Service[Any] = new Service[Any] {
      override def info(s : String): UIO[Unit] = {
        ZIO.effectTotal {
          println(s)
        }
      }

      override def error(s: String): UIO[Unit] = {
        ZIO.effectTotal {
          println(s)
        }
      }

      override def error(e: Throwable): UIO[Unit] = {
        ZIO.effectTotal {
          println(e.getMessage)
          e.printStackTrace()
        }
      }
    }
  }
}
