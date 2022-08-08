package controllers

import models.StatusInfo
import services.StatusInfoService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class APIController @Inject() (
    val controllerComponents: ControllerComponents,
    statInfo: StatusInfoService
) extends BaseController {

  /** method that returns a json object with the following information:
    * { "service" : "pass-bakery"
    *   "environment" : string (production or development run, based on play fw env config)
    *   "serverTime" : string (local time from dateTime library in ISO 8601 format)
    * }
    */
  def getStatus() = Action { implicit request: Request[AnyContent] =>
    Ok(statInfo.getUserStatus())
  }
}
