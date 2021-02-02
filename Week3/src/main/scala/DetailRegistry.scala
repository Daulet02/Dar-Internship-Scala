import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable

final case class Detail(id: String, name: String)
final case class Details(details: immutable.Seq[Detail])

object DetailRegistry {
  sealed trait Command
  final case class GetDetails(replyTo: ActorRef[Details]) extends Command

  final case class CreateDetail(detail: Detail, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetDetail(id: String, replyTo: ActorRef[GetDetailResponse]) extends Command
  final case class DeleteDetail(id: String, replyTo: ActorRef[ActionPerformed]) extends Command
  //final case class UpdateDetail(id: Int) extends Command

  final case class GetDetailResponse(maybeDetail: Option[Detail])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(details: Set[Detail]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetDetails(replyTo) =>
        replyTo ! Details(details.toSeq)
        Behaviors.same
      case GetDetail(id, replyTo) =>
        replyTo ! GetDetailResponse(details.find(_.id == id))
        Behaviors.same
      case CreateDetail(detail, replyTo) =>
        replyTo ! ActionPerformed(s"Auto Detail ${detail.name} has been created.")
        registry(details + detail)
      case DeleteDetail(id, replyTo) =>
        replyTo ! ActionPerformed(s"Detail with id:${id} has been deleted.")
        registry(details.filterNot(_.id == id))
    }
}
