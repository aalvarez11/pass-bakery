package controllers

import DAO.BakeryDatabase
import services.StatusInfoService

import javax.inject._
import play.api.mvc._

import scala.concurrent.ExecutionContext

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

  def getProduct(id: String) = Action.async {
    implicit request: Request[AnyContent] =>
      bakeryDB.getProductById(id).map {
        case None => NotFound("No Product Found")
        case Some(myProduct) =>
          Ok(
            myProduct.name + ", qty: " + myProduct.quantity + ", price: " + myProduct.price +
              ", created at: " + myProduct.createdAt + ", updated at: " + myProduct.updatedAt
          )
      }
  }

  def getAllProducts() = Action.async { implicit request: Request[AnyContent] =>
    bakeryDB.getAllProducts.map { allProducts =>
      Ok(allProducts)
    }
  }
}
