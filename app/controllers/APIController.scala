package controllers

import DAO.BakeryDatabase
import play.api.libs.json._
import services.StatusInfoService

import javax.inject._
import play.api.mvc._

import java.util.UUID
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
          val passProductJson = newProduct.validate[ProductUpdateRequest]
          if (passProductJson.isSuccess) {
            val rowsUpdated = bakeryDB.createProduct(passProductJson.get)
            if (rowsUpdated == 1) {
              Ok("New record added")
            } else {
              BadRequest(
                "Couldn't create the record with the given data, please make sure the body contains " +
                  "the following information: name, quantity, price."
              )
            }
          } else {
            BadRequest("Json was incorrectly structured")
          }
        }
      }
    }
  }

  def getProduct(id: UUID) = Action.async {
    implicit request: Request[AnyContent] =>
      bakeryDB.getProductById(id).map {
        case None => NotFound("No Product Found")
        case Some(myProduct) =>
          Ok(
            Json.prettyPrint(Json.toJson(myProduct))
          )
      }
  }

  def updateProduct(id: UUID) = Action.async {
    implicit request: Request[AnyContent] =>
      /** 1. get what is going to be updated in the request body
        *  2. see if the item exists in the database, if not give a 404
        *  3. found product -> start the update process:
        *  4. see if the request body had json to process
        *  5. validate the json to make sure it matches the case class
        *  (this assumes non-update values will be passed as empty)
        *  6. validate success means pass the id and body json to update method
        *  7. only the record with the matching id should update, method should return 1 & 200/OK
        *  (in case something other than 1 returns, respond with a 500/ServerError
        *  8. validate fail means something in the json was off
        */
      val requestJson = request.body.asJson
      bakeryDB.getProductById(id).map {
        case None => NotFound("No Product Found")
        case Some(myProduct) => {
          requestJson match {
            case None => BadRequest("No request body found")
            case Some(updateProduct) => {
              val productJsonResult =
                updateProduct.validate[ProductUpdateRequest]
              productJsonResult match {
                case JsSuccess(productUpdateRequest, path) => {
                  val rowsUpdated =
                    bakeryDB.updateProduct(id, productUpdateRequest)
                  if (rowsUpdated == 1) {
                    Ok("Record updated")
                  } else {
                    InternalServerError("Couldn't update the record")
                  }
                }
                case JsError(exception) =>
                  BadRequest(
                    "Json includes mistyped data, please double check the data types of information you are passing"
                  )
              }
            }
          }
        }
      }
  }

  def deleteProduct(id: UUID) = Action.async {
    implicit request: Request[AnyContent] =>
      bakeryDB.getProductById(id).map {
        case None => NotFound("No product with that id found")
        case Some(myProduct) =>
          val rowsDeleted = bakeryDB.deleteProduct(id)
          if (rowsDeleted == 1) {
            Ok("Record deleted")
          } else {
            InternalServerError("Couldn't delete the record")
          }
      }
  }

  def getAllProducts() = Action.async { implicit request: Request[AnyContent] =>
    bakeryDB.getAllProducts.map { allProducts =>
      Ok(allProducts)
    }
  }
}

case class ProductUpdateRequest(
    name: Option[String],
    quantity: Option[Int],
    price: Option[Double]
)

object ProductUpdateRequest {
  implicit val requestReads: Format[ProductUpdateRequest] =
    Json.format[ProductUpdateRequest]
}
