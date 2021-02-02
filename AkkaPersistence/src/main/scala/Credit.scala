import scala.concurrent.duration._
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.RetentionCriteria
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior


object Credit {
  sealed trait Command
  final case class Accept(id: String, replyTo: ActorRef[StatusReply[Summary]]) extends Command
  final case class Pay(money: Int, replyTo: ActorRef[StatusReply[Summary]]) extends Command
  final case class CreateContract(id: String, replyTo: ActorRef[StatusReply[Summary]]) extends Command
  final case class GiveCredit(id: String, replyTo: ActorRef[StatusReply[Summary]]) extends Command
  final case class Summary(checkedOut: Boolean) extends CborSerializable

  sealed trait Event
  final case class Accepted(id: String) extends Event
  final case class Payed(money: Int) extends Event
  final case class DocumentCreated(id: String) extends Event
  final case class CreditGave(id: String) extends Event

  final case class State(Task_inProgress: Option[String]) {
//    def toSummary: Summary =
//      Summary()
  }

  def apply(persistenceId: PersistenceId): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = persistenceId,
      emptyState = State(None),
      commandHandler = (state, command) => onCommand(state, command),
      eventHandler = (state, event) => applyEvent(state, event))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(1.second, 30.seconds, 0.2))

  private def onCommand(state: State, command: Command): Effect[Event, State] = {
    state.Task_inProgress match {
      case None =>
        command match {
          case Accept(id, replyTo: ActorRef[Summary]) => Effect.persist(Accepted(id))
          case _ => Effect.unhandled
        }

      case Some(inProgress) =>
        command match {
          case Accept(id, replyTo: ActorRef[Summary]) =>
            Effect.persist(Accepted(id))
            Effect.stash()

          case Pay(money, replyTo: ActorRef[Summary]) =>
            if (money >= 10000)
              Effect.persist(Payed(money))
            else
              //StatusReply.error("Not enough money to pay!").
            Effect.stash()

          case CreateContract(id, replyTo: ActorRef[Summary]) =>
            if (inProgress == id)
              Effect.persist(DocumentCreated(id)).thenUnstashAll()
            else
              Effect.stash()

          case GiveCredit(id, replyTo: ActorRef[Summary]) =>
            if (inProgress == id)
              Effect.persist(CreditGave(id))
            else
              Effect.stash()
        }
    }
  }

  private def applyEvent(state: State, event: Event): State = {
    event match {
      case Accepted(id)       => State(Option(id))
      case Payed(_)           => state
      case DocumentCreated(_) => State(None)
      case CreditGave(_)      => State(None)
    }
  }
}
