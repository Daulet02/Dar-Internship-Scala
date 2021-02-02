
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try


object Main {

  def main(args: Array[String]): Unit = {

    implicit val log: Logger = LoggerFactory.getLogger(getClass)

    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val details: Seq[Detail] = Seq(
        Detail("1", "detail 1"),
        Detail("2", "detail 2"),
        Detail("3", "detail 3"),
        Detail("4", "detail 4")
      )

      val AutoDetailRepository = new InMemoryAutoDetailRepository(details)(context.executionContext)
      val router = new MyRouter(AutoDetailRepository)(context.system, context.executionContext)

      val host = "localhost"
      val port = Try(System.getenv("PORT")).map(_.toInt).getOrElse(9000)

      Server.startHttpServer(router.route, host, port)(context.system, context.executionContext)
      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }

}