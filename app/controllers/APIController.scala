package controllers

import DAO.BakeryDatabase
import services.StatusInfoService

import javax.inject._
import play.api.mvc._

import scala.concurrent.ExecutionContext

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class APIController @Inject() (
    val controllerComponents: ControllerComponents,
    statInfo: StatusInfoService,
    bakeryDB: BakeryDatabase,
    implicit val executionContext: ExecutionContext
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

  def getDatabaseTables() = Action.async {
    implicit request: Request[AnyContent] =>
      bakeryDB.getDatabaseTables.map { databaseTables =>
        Ok(databaseTables)
      }
  }
}
