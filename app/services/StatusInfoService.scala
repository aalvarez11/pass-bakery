package services

import models.StatusInfo
import play.api.Environment
import play.api.http.Writeable
import play.api.libs.json.{JsValue, Json, OWrites}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class StatusInfoService @Inject()(env: Environment) {
  def getUserStatus(): JsValue = {

    val userEnvironment = env.mode.toString
    val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val userInfo = StatusInfo("pass-bakery", userEnvironment, formattedTimestamp)
    val statusJson : JsValue = Json.toJson(userInfo)
    statusJson
  }
}
