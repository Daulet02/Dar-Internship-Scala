import akka.actor.typed.ActorSystem
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import  io.circe.generic.auto._
import akka.http.scaladsl.server.{Directives, Route}

import scala.concurrent.ExecutionContext

trait Router {
  def route: Route
}

class MyRouter(val detailRepository: AutoDetailRepository)(implicit system: ActorSystem[_],  ex:ExecutionContext)
  extends  Router
    with Directives
    with HealthCheckRoute
    with ValidatorDirectives
    with AutoDetailDirectives {

  def detail = {
    pathPrefix("details") {
      concat(
        pathEndOrSingleSlash {
          concat(
            get {
              complete(detailRepository.all())
            },
            post {
              entity(as[CreateDetail]) { createDetail =>
                validateWith(CreateAutoDetailValidator)(createDetail) {
                  handleWithGeneric(detailRepository.create(createDetail)) { detail =>
                    complete(detail)
                  }
                }
              }
            }

          )
        } ~
          path(Segment) { id =>
            concat(
              put {
                entity(as[UpdateDetail]) { updateDetail =>
                  validateWith(UpdateAutoDetailValidator)(updateDetail) {
                    handle(detailRepository.update(id, updateDetail)) {
                      case AutoDetailRepository.AutoDetailNotFound(_) =>
                        ApiError.detailNotFound(id)
                      case _ =>
                        ApiError.generic
                    } { detail =>
                      complete(detail)
                    }
                  }
                }
              },
              get {
                complete(detailRepository.getDetail(id))
              },
              delete {
                complete(detailRepository.deleteDetail(id))
              }
            )
          }
      )
    }
  }

  override def route = {
    concat(
      healthCheck,
      detail
    )
  }
}