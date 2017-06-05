package model
import java.time.Instant

sealed trait Model

case class Talk(time: Instant,
                speaker: String,
                speakerCompany: Option[String],
                subject: String,
                twitterHandle: Option[String])
    extends Model
