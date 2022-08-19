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
        case None => BadRequest("No request body found")
        case Some(newProduct) => {
          val passProductJson = newProduct.validate[productUpdateRequest]
          if (passProductJson.isSuccess) {
            val rowsUpdated = bakeryDB.createProduct(newProduct)
            if (rowsUpdated == 1) {
              Ok("New record added")
            } else {
              InternalServerError("Couldn't create the record")
            }
          } else {
            BadRequest("Json was incorrectly structured")
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

  def updateProduct(id: String) = Action.async {
    implicit request: Request[AnyContent] =>
      bakeryDB.getProductById(id).map {
        case None => NotFound("No Product Found")
        case Some(myProduct) =>
          val requestJson = request.body.asJson
          Future {
            requestJson match {
              case None => BadRequest("No request body found")
              case Some(updateProduct) => {
                val ProductJsonResult =
                  updateProduct.validate[productUpdateRequest]
                if (ProductJsonResult.isSuccess) {
                  val rowsUpdated =
                    bakeryDB.updateProduct(myProduct.id, updateProduct)
                  if (rowsUpdated == 1) {
                    Ok("Record updated")
                  } else {
                    InternalServerError("Couldn't update the record")
                  }
                } else {
                  BadRequest("Json was incorrectly structured")
                }
              }
            }
          }
      }
  }

  def getAllProducts() = Action.async { implicit request: Request[AnyContent] =>
    bakeryDB.getAllProducts.map { allProducts =>
      Ok(allProducts)
    }
  }
}

case class productUpdateRequest(
    name: String,
    quantity: Int,
    price: Double
)

object productUpdateRequest {
  implicit val requestReads: Format[productUpdateRequest] =
    Json.format[productUpdateRequest]
}
