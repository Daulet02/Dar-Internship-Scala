package Entity
import Command.{AddToCartCommand, BookShopCommand, CreateBookCommand, DeleteBookFromCartCommand, PayCommand, SearchBookCommand}
import Event.{AddedToCartEvent, BookShopEvent, CreateBookEvent, DeletedBookFromCartEvent, FindBookEvent, ReturnedBookEvent, SellBookEvent}
import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object BookShopEntity {
  case class Book(name: Option[String] = None,
                  bookId: Option[String] = None,
                  author: Option[String] = None)

  object BookShop {
    def empty = new Book()
  }

  trait State

  trait BookShopEntityState

  object BookShopEntityState {

    case object FIND extends BookShopEntityState

    case object ADDTOCART extends BookShopEntityState

    case object DELETEBOOK extends BookShopEntityState

    case object INIT extends BookShopEntityState

    case object PAY extends BookShopEntityState

    case object FINISH extends BookShopEntityState

    case object RETURNBOOK extends BookShopEntityState

  }

  case class StateHolder(content: Book, state: BookShopEntityState) {

    def update(event: BookShopEvent): StateHolder = event match {
      case evt: CreateBookEvent => {
        copy(
          content = content.copy(
            bookId = Some(evt.bookId),
            name = Some(evt.name),
            author = Some(evt.author)
          ),
          state = BookShopEntityState.FIND
        )
      }
      case evt: FindBookEvent => {
        copy(
          content = content.copy(
            bookId = Some(evt.bookId)
          ),
          state = BookShopEntityState.ADDTOCART
        )
      }
      case evt: AddedToCartEvent => {
        copy(
          content = content.copy(
            bookId = Some(evt.bookId)
          ),
          state = BookShopEntityState.PAY
        )
      }

      case evt: SellBookEvent => {
        copy(
          state = BookShopEntityState.FINISH
        )
      }

      case evt: ReturnedBookEvent => {
        copy(
          content = content.copy(
            bookId = Some(evt.bookId)
          ),
          state = BookShopEntityState.RETURNBOOK
        )
      }
    }
  }


  object StateHolder {
    def empty: StateHolder = StateHolder(content = BookShop.empty, state = BookShopEntityState.INIT)
  }

  val entityKey: EntityTypeKey[BookShopCommand] = EntityTypeKey[BookShopCommand]("Book Shop")

  def apply(bookId: String): Behavior[BookShopCommand] = {
    EventSourcedBehavior[BookShopCommand, BookShopEvent, StateHolder](
      persistenceId = PersistenceId(entityKey.name, bookId),
      StateHolder.empty,
      (state, command) => commandHandler(bookId, state, command),
      (state, event) => handleEvent(state, event)
    )
  }

  def commandHandler(bookId: String, state: StateHolder, command: BookShopCommand): Effect[BookShopEvent, StateHolder] = {
    command match {

      case cmd: CreateBookCommand => {
        state.state match {
          case BookShopEntityState.INIT => {
            val evt = CreateBookEvent(
              bookId = cmd.bookId,
              name = cmd.name,
              author = cmd.author
            )
            Effect.persist(evt)
          }
          case _ => throw new RuntimeException("Error")
        }
      }

      case cmd: SearchBookCommand => {
        state.state match {
          case BookShopEntityState.FIND => {
            val evt = FindBookEvent(
              bookId = cmd.bookId,
              name = cmd.name[Option],
              author = cmd.author[Option]
            )
            Effect.persist(evt)
          }
        }
      }

      case cmd: AddToCartCommand => {
        state.state match {
          case BookShopEntityState.ADDTOCART => {
            val evt = AddedToCartEvent(
              bookId = cmd.bookId
            )
            Effect.persist(evt)
          }
        }
      }

      case cmd: DeleteBookFromCartCommand => {
        state.state match {
          case BookShopEntityState.DELETEBOOK => {
            val evt = DeletedBookFromCartEvent(
              bookId = cmd.bookId[Option]
            )
            Effect.persist(evt)
          }
        }
      }

      case cmd: PayCommand => {
        state.state match {
          case BookShopEntityState.PAY => {
            val evt = SellBookEvent(
              bookId = cmd.bookId
            )
            Effect.persist(evt)
          }
        }
      }
    }
  }

  def handleEvent(state: StateHolder, event: BookShopEvent): StateHolder = {
    state.update(event)
  }
}
