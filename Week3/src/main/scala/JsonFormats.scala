import DetailRegistry.ActionPerformed
import spray.json.DefaultJsonProtocol


object JsonFormats {
  import DefaultJsonProtocol._

  implicit val detailJsonFormat = jsonFormat2(Detail)
  implicit val detailsJsonFormat = jsonFormat1(Details)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
