package controllers

import models.StatusInfo
import java.time.LocalDateTime
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Environment._

import java.time.format.DateTimeFormatter

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class APIController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {


  /**
   * method that returns a json object with the following information:
   * { "service" : "pass-bakery"
   *   "environment" : string (production or development run, based on play fw env config)
   *   "serverTime" : string (local time from dateTime library in ISO 8601 format)
   * }
   */
  def getStatus() = Action { implicit request: Request[AnyContent] =>
    implicit val statusWrites: OWrites[StatusInfo] = Json.writes[StatusInfo]
    val userEnvironment = play.api.Mode.toString
    val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val userInfo = StatusInfo("pass-bakery", userEnvironment, formattedTimestamp)
    val statusJson : JsValue = Json.toJson(userInfo)

    Ok(statusJson)
  }
}
