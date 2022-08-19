package controllers

import DAO.BakeryDatabase
import play.api.libs.json._
import services.StatusInfoService

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

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
    Ok(Json.prettyPrint(statInfo.getUserStatus()))
  }

  def getDatabaseTables() = Action.async {
    implicit request: Request[AnyContent] =>
      bakeryDB.getDatabaseTables.map { databaseTables =>
        Ok(databaseTables)
      }
  }

  def createProduct() = Action.async { implicit request: Request[AnyContent] =>
    val reqJson = request.body.asJson
    Future {
      reqJson match {
        case None => BadRequest("no request body found")
        case Some(newProduct) => {
          val rowsUpdated = bakeryDB.createProduct(newProduct)
          if (rowsUpdated == 1) {
            Created("New record added")
          } else {
            InternalServerError("Couldn't create the row")
          }
        }
      }
    }
  }

  def getProduct(id: String) = Action.async {
    implicit request: Request[AnyContent] =>
      bakeryDB.getProductById(id).map {
        case None => NotFound("No Product Found")
        case Some(myProduct) =>
          Ok(
            Json.prettyPrint(Json.toJson(myProduct))
          )
      }
  }

  def getAllProducts() = Action.async { implicit request: Request[AnyContent] =>
    bakeryDB.getAllProducts.map { allProducts =>
      Ok(allProducts)
    }
  }
}
