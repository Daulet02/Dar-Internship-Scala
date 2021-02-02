trait Validator[T] {
  def validate(t: T): Option[ApiError]
}

object CreateAutoDetailValidator extends Validator[CreateDetail] {
  def validate(createAddressBook: CreateDetail): Option[ApiError] =
    if (createAddressBook.name.isEmpty)
      Some(ApiError.emptyTitleField)
    else
      None
}

object UpdateAutoDetailValidator extends Validator[UpdateDetail] {
  def validate(updateDetail: UpdateDetail): Option[ApiError] =
    if (updateDetail.id.exists(_.isEmpty))
      Some(ApiError.emptyTitleField)
    else
      None
}