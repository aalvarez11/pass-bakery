package models

import play.api.libs.json._

case class StatusInfo(service: String, environment: String, serverTime: String)

object StatusInfo {
  implicit val statusWrites: OWrites[StatusInfo] = Json.writes[StatusInfo]
}
