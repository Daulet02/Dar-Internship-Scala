import java.util.UUID

import AutoDetailRepository.AutoDetailNotFound

import scala.concurrent.{ExecutionContext, Future}

trait AutoDetailRepository {
  def all(): Future[Seq[Detail]]
  def create(createDetail: CreateDetail): Future[Detail]
  def update(id: String, updateDetail: UpdateDetail): Future[Detail]
  def getDetail(id: String): Future[Option[Detail]]
  def deleteDetail(id: String): Future[Unit]
}


object AutoDetailRepository {
  final case class AutoDetailNotFound(id: String) extends Exception(s"Auto Detail with id $id not found.")
}

class InMemoryAutoDetailRepository(initialDetails: Seq[Detail] = Seq.empty)(implicit ec: ExecutionContext) extends AutoDetailRepository {

  private var Details: Vector[Detail] = initialDetails.toVector

  override def all(): Future[Seq[Detail]] = Future.successful(Details)

  override def create(createDetail: CreateDetail): Future[Detail] = Future.successful {
    val detail = Detail(
      id = UUID.randomUUID().toString,
      name = createDetail.name
    )
    Details = Details :+ detail
    detail
  }

  override def update(id: String, updateDetail: UpdateDetail): Future[Detail] = {
    Details.find(_.id == id) match {
      case Some(foundDetail) =>
        val newDetail = updateHelper(foundDetail, updateDetail)
        Details = Details.map(t => if (t.id == id) newDetail else t)
        Future.successful(newDetail)
      case None =>
        Future.failed(AutoDetailNotFound(id))
    }
  }

  private def updateHelper(detail: Detail, updateDetail: UpdateDetail): Detail = {
    val t1 = updateDetail.id.map(id => detail.copy(id = id)).getOrElse(detail)
    val t2 = updateDetail.name.map(name => t1.copy(name = name)).getOrElse(t1)
    t2
  }

  override def getDetail(id: String): Future[Option[Detail]] = Future.successful {
    Details.find(_.id == id)
  }

  override def deleteDetail(id: String): Future[Unit] = Future.successful {
    Details = Details.filterNot(_.id == id)
  }

}