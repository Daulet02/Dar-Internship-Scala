package Command

trait BookShopCommand {
  def bookId: String
}

case class CreateBookCommand(bookId: String,
                             name: String,
                             author: String) extends BookShopCommand

case class SearchBookCommand(bookId: String, name: String, author: String) extends BookShopCommand

case class AddToCartCommand(bookId: String) extends BookShopCommand

case class DeleteBookFromCartCommand(bookId: String) extends BookShopCommand

case class PayCommand(bookId: String) extends BookShopCommand

case class ReturnBookCommand(bookId: String) extends BookShopCommand



