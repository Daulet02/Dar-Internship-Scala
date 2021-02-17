package Event

trait BookShopEvent

case class CreateBookEvent(bookId: String,
                           name: String,
                           author: String) extends BookShopEvent

case class FindBookEvent(bookId: String,
                         name: Option[String],
                         author: Option[String]) extends BookShopEvent

case class AddedToCartEvent(bookId: String) extends BookShopEvent

case class DeletedBookFromCartEvent(bookId: Option[String]) extends BookShopEvent

case class SellBookEvent(bookId: String) extends BookShopEvent

case class ReturnedBookEvent(bookId: String) extends BookShopEvent
