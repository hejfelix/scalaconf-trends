package model

sealed trait Model

case class Talk(time: String,
                speaker: String,
                speakerCompany: Option[String],
                subject: String,
                twitterHandle: Option[String])
    extends Model
