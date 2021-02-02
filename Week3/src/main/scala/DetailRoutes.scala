import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import DetailRegistry._
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout


class DetailRoutes(detailRegistry: ActorRef[DetailRegistry.Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getDetails(): Future[Details] =
    detailRegistry.ask(GetDetails)
  def getDetail(id: String): Future[GetDetailResponse] =
    detailRegistry.ask(GetDetail(id, _))
  def createDetail(detail: Detail): Future[ActionPerformed] =
    detailRegistry.ask(CreateDetail(detail, _))
  def deleteDetail(id: String): Future[ActionPerformed] =
    detailRegistry.ask(DeleteDetail(id, _))


  val detailRoutes: Route =
    pathPrefix("details") {
      concat(
        pathEnd {
          concat(
            get {
              complete(getDetails())
            },
            post {
              entity(as[Detail]) { detail =>
                onSuccess(createDetail(detail)) { performed =>
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        path(Segment) { id =>
          concat(
            get {
              rejectEmptyResponse {
                onSuccess(getDetail(id)) { response =>
                  complete(response.maybeDetail)
                }
              }
            },
            delete {
              //#users-delete-logic
              onSuccess(deleteDetail(id)) { performed =>
                complete((StatusCodes.OK, performed))
              }
              //#users-delete-logic
            })
        }
      )
    }
}
