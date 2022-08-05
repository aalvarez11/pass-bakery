package controllers

import models.StatusInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class APIController @Inject()(val controllerComponents: ControllerComponents, env: Environment) extends BaseController {

  /**
   * method that returns a json object with the following information:
   * { "service" : "pass-bakery"
   *   "environment" : string (production or development run, based on play fw env config)
   *   "serverTime" : string (local time from dateTime library in ISO 8601 format)
   * }
   */

  def getStatus() = Action { implicit request: Request[AnyContent] =>

    val userEnvironment = env.mode.toString
    val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val userInfo = StatusInfo("pass-bakery", userEnvironment, formattedTimestamp)
    val statusJson : JsValue = Json.toJson(userInfo)

    Ok(statusJson)
  }
}
