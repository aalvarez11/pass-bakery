package controllers

import DAO.BakeryDatabase
import models.{BakeryTransactor, DatabaseExecutionContext}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Environment
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import services.StatusInfoService

import java.util.UUID
import javax.inject.Inject

class APIControllerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting {

  "APIController GET" should {

    "return the status of current environment" in {
      val controller = inject[APIController]
      val bakeryStatus = {
        controller.getStatus().apply(FakeRequest(GET, "/pass-bakery/status"))
      }
      status(bakeryStatus) mustBe OK
      contentAsString(bakeryStatus) must include("pass-bakery")
    }

    "return the json of the specified product" in {
      val controller = inject[APIController]
      val productResult = controller
        .getProduct(UUID.fromString("e69cf7fa-7b16-4636-a859-10d4675cfcc6"))
        .apply(FakeRequest(GET, "/rest/bakery/product/:id"))
      status(productResult) mustBe OK
      contentAsString(productResult) must include("tres leches")
    }

    "return 'product not found' for nonexistent item" in {
      val controller = inject[APIController]
      val productResult = controller
        .getProduct(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        .apply(FakeRequest(GET, "/rest/bakery/product/:id"))
      status(productResult) mustBe NOT_FOUND
      contentAsString(productResult) must include("No Product Found")
    }

    "return all items in the database" in {
      val controller = inject[APIController]
      val allProducts = controller
        .getAllProducts()
        .apply(FakeRequest(GET, "/rest/bakery/all-products"))
      status(allProducts) mustBe OK
      contentAsString(allProducts) must include("conchas")
      contentAsString(allProducts) must include("pastel tres leches")
      contentAsString(allProducts) must include("strawberry-frosted donuts")
    }
  }

  "APIController POST" should {
    "return bad request if no body is present in the request" in {
      val controller = inject[APIController]
      val noBody = controller
        .createProduct()
        .apply(FakeRequest(POST, "/rest/bakery/product"))
      status(noBody) mustBe BAD_REQUEST
      contentAsString(noBody) must include("No request body found")
    }

    "return bad request if body is missing price" in {
      val missingPrice: JsValue = JsObject(
        Seq(
          "name" -> JsString("test"),
          "quantity" -> JsNumber(5)
        )
      )
      val controller = inject[APIController]
      val incorrectBody = controller
        .createProduct()
        .apply(
          FakeRequest(POST, "/rest/bakery/product").withJsonBody(missingPrice)
        )
      status(incorrectBody) mustBe BAD_REQUEST
      contentAsString(incorrectBody) must include(
        "Couldn't create the record with the given data"
      )
    }

    "return bad request if body is missing name" in {
      val missingPrice: JsValue = JsObject(
        Seq(
          "price" -> JsNumber(2.00),
          "quantity" -> JsNumber(5)
        )
      )
      val controller = inject[APIController]
      val incorrectBody = controller
        .createProduct()
        .apply(
          FakeRequest(POST, "/rest/bakery/product").withJsonBody(missingPrice)
        )
      status(incorrectBody) mustBe BAD_REQUEST
      contentAsString(incorrectBody) must include(
        "Couldn't create the record with the given data"
      )
    }

    "return bad request if body is missing quantity" in {
      val missingPrice: JsValue = JsObject(
        Seq(
          "name" -> JsString("test"),
          "price" -> JsNumber(3.00)
        )
      )
      val controller = inject[APIController]
      val incorrectBody = controller
        .createProduct()
        .apply(
          FakeRequest(POST, "/rest/bakery/product").withJsonBody(missingPrice)
        )
      status(incorrectBody) mustBe BAD_REQUEST
      contentAsString(incorrectBody) must include(
        "Couldn't create the record with the given data"
      )
    }

    "return bad request if a body member is wrongly typed" in {
      val missingPrice: JsValue = JsObject(
        Seq(
          "name" -> JsNumber(1.00),
          "quantity" -> JsNumber(5),
          "price" -> JsNumber(5.00)
        )
      )
      val controller = inject[APIController]
      val incorrectBody = controller
        .createProduct()
        .apply(
          FakeRequest(POST, "/rest/bakery/product").withJsonBody(missingPrice)
        )
      status(incorrectBody) mustBe BAD_REQUEST
      contentAsString(incorrectBody) must include(
        "Json was incorrectly structured"
      )
    }

    "return product created if body follows correct structure" in {
      val goodJson: JsValue = JsObject(
        Seq(
          "name" -> JsString("test"),
          "quantity" -> JsNumber(5),
          "price" -> JsNumber(5.00)
        )
      )
      val controller = inject[APIController]
      val correctBody = controller
        .createProduct()
        .apply(FakeRequest(POST, "/rest/bakery/product").withJsonBody(goodJson))
      status(correctBody) mustBe OK
      contentAsString(correctBody) must include("New record added")
    }

    "return product created if body is correctly structured but is oddly ordered" in {
      val goodJson: JsValue = JsObject(
        Seq(
          "price" -> JsNumber(5.00),
          "name" -> JsString("test"),
          "quantity" -> JsNumber(5)
        )
      )
      val controller = inject[APIController]
      val correctBody = controller
        .createProduct()
        .apply(FakeRequest(POST, "/rest/bakery/product").withJsonBody(goodJson))
      status(correctBody) mustBe OK
      contentAsString(correctBody) must include("New record added")
    }

    "return product created if body contains the fields desired but also contains other data" in {
      val extraJson: JsValue = JsObject(
        Seq(
          "price" -> JsNumber(5.00),
          "name" -> JsString("test"),
          "quantity" -> JsNumber(5),
          "location" -> JsString("1234 W Street")
        )
      )
      val controller = inject[APIController]
      val correctBody = controller
        .createProduct()
        .apply(
          FakeRequest(POST, "/rest/bakery/product").withJsonBody(extraJson)
        )
      status(correctBody) mustBe OK
      contentAsString(correctBody) must include("New record added")
    }
  }

  "APIController PUT" should {
    "return not found if the id is not present in the db" in {
      val okJson: JsValue = JsObject(
        Seq(
          "name" -> JsString("test"),
          "price" -> JsNumber(5.00),
          "quantity" -> JsNumber(5)
        )
      )
      val controller = inject[APIController]
      val noIdFound = controller
        .updateProduct(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(okJson)
        )
      status(noIdFound) mustBe NOT_FOUND
      contentAsString(noIdFound) must include("No Product Found")
    }

    "return bad request if no body is present in the request" in {
      val controller = inject[APIController]
      val noBody = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(FakeRequest(PUT, "/rest/bakery/product/:id"))
      status(noBody) mustBe BAD_REQUEST
      contentAsString(noBody) must include("No request body found")
    }

    "return ok if an existing product was successfully updated with all 3 parameters" in {
      val okJson: JsValue = JsObject(
        Seq(
          "name" -> JsString("test"),
          "price" -> JsNumber(5.00),
          "quantity" -> JsNumber(5)
        )
      )
      val controller = inject[APIController]
      val successfulUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(okJson)
        )
      status(successfulUpdate) mustBe OK
      contentAsString(successfulUpdate) must include("Record updated")
    }

    "return ok if an existing product was successfully updated with 2 parameters: name and price" in {
      val okJson: JsValue = JsObject(
        Seq(
          "name" -> JsString("test"),
          "price" -> JsNumber(5.00)
        )
      )
      val controller = inject[APIController]
      val successfulUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(okJson)
        )
      status(successfulUpdate) mustBe OK
      contentAsString(successfulUpdate) must include("Record updated")
    }

    "return ok if an existing product was successfully updated with 2 parameters: name and quantity" in {
      val okJson: JsValue = JsObject(
        Seq(
          "name" -> JsString("test"),
          "quantity" -> JsNumber(5)
        )
      )
      val controller = inject[APIController]
      val successfulUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(okJson)
        )
      status(successfulUpdate) mustBe OK
      contentAsString(successfulUpdate) must include("Record updated")
    }

    "return ok if an existing product was successfully updated with 2 parameters: price and quantity" in {
      val okJson: JsValue = JsObject(
        Seq(
          "price" -> JsNumber(5.00),
          "quantity" -> JsNumber(5)
        )
      )
      val controller = inject[APIController]
      val successfulUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(okJson)
        )
      status(successfulUpdate) mustBe OK
      contentAsString(successfulUpdate) must include("Record updated")
    }

    "return ok if an existing product was successfully updated with 1 parameter: name" in {
      val okJson: JsValue = JsObject(
        Seq(
          "name" -> JsString("test")
        )
      )
      val controller = inject[APIController]
      val successfulUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(okJson)
        )
      status(successfulUpdate) mustBe OK
      contentAsString(successfulUpdate) must include("Record updated")
    }

    "return ok if an existing product was successfully updated with 1 parameter: price" in {
      val okJson: JsValue = JsObject(
        Seq(
          "price" -> JsNumber(5.00)
        )
      )
      val controller = inject[APIController]
      val successfulUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(okJson)
        )
      status(successfulUpdate) mustBe OK
      contentAsString(successfulUpdate) must include("Record updated")
    }

    "return ok if an existing product was successfully updated with 1 parameter: quantity" in {
      val okJson: JsValue = JsObject(
        Seq(
          "quantity" -> JsNumber(5)
        )
      )
      val controller = inject[APIController]
      val successfulUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(okJson)
        )
      status(successfulUpdate) mustBe OK
      contentAsString(successfulUpdate) must include("Record updated")
    }

    "return bad request if a parameter is mistyped" in {
      val badJson: JsValue = JsObject(
        Seq(
          "quantity" -> JsString("asdf")
        )
      )
      val controller = inject[APIController]
      val mistypedUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(badJson)
        )
      status(mistypedUpdate) mustBe BAD_REQUEST
      contentAsString(mistypedUpdate) must include(
        "Couldn't update the record with the given data, please make sure the body contains any"
      )
    }

    "return bad request if 2 parameters are mistyped" in {
      val badJson: JsValue = JsObject(
        Seq(
          "name" -> JsNumber(4),
          "quantity" -> JsString("asdf")
        )
      )
      val controller = inject[APIController]
      val mistypedUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(badJson)
        )
      status(mistypedUpdate) mustBe BAD_REQUEST
      contentAsString(mistypedUpdate) must include(
        "Couldn't update the record with the given data, please make sure the body contains any"
      )
    }

    "return bad request if all 3 parameters are mistyped" in {
      val badJson: JsValue = JsObject(
        Seq(
          "name" -> JsNumber(4),
          "price" -> JsBoolean(true),
          "quantity" -> JsString("asdf")
        )
      )
      val controller = inject[APIController]
      val mistypedUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(badJson)
        )
      status(mistypedUpdate) mustBe BAD_REQUEST
      contentAsString(mistypedUpdate) must include(
        "Couldn't update the record with the given data, please make sure the body contains any"
      )
    }

    "return bad request if 1 parameter is mistyped and all 3 are present" in {
      val badJson: JsValue = JsObject(
        Seq(
          "name" -> JsString("new name"),
          "price" -> JsNumber(1.00),
          "quantity" -> JsString("asdf")
        )
      )
      val controller = inject[APIController]
      val mistypedUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(badJson)
        )
      status(mistypedUpdate) mustBe BAD_REQUEST
      contentAsString(mistypedUpdate) must include(
        "Couldn't update the record with the given data, please make sure the body contains any"
      )
    }

    "return bad request if a body with nothing inside is given" in {
      val emptyJson: JsValue = JsObject(Seq())
      val controller = inject[APIController]
      val emptyUpdate = controller
        .updateProduct(UUID.fromString("0a776c48-1ef0-4d2e-8fab-81d074b0c8d4"))
        .apply(
          FakeRequest(PUT, "/rest/bakery/product/:id").withJsonBody(emptyJson)
        )
      status(emptyUpdate) mustBe BAD_REQUEST
      contentAsString(emptyUpdate) must include(
        "Couldn't update the record, please make sure the body contains"
      )
    }
  }

  "APIController DELETE" should {
    "return not found if the product to be deleted isn't in the product table" in {
      val controller = inject[APIController]
      val deleteNotFound = controller
        .deleteProduct(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        .apply(FakeRequest(DELETE, "/rest/bakery/product/:id"))
      status(deleteNotFound) mustBe NOT_FOUND
      contentAsString(deleteNotFound) must include(
        "No product with that id found"
      )
    }

    "return ok if an existing product was deleted" in {
      val controller = inject[APIController]
      val deleteSuccess = controller
        .deleteProduct(UUID.fromString("e69cf7fa-7b16-4636-a859-10d4675cfcc6"))
        .apply(FakeRequest(DELETE, "/rest/bakery/product/:id"))
      status(deleteSuccess) mustBe OK
      contentAsString(deleteSuccess) must include("Record deleted")
    }
  }
}
