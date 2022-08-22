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
        .getProduct("e69cf7fa-7b16-4636-a859-10d4675cfcc6")
        .apply(FakeRequest(GET, "/rest/bakery/product/:id"))
      status(productResult) mustBe OK
      contentAsString(productResult) must include("tres leches")
    }

    "return 'product not found' for nonexistent item" in {
      val controller = inject[APIController]
      val productResult = controller
        .getProduct("asdf-qwer-jkl-yuio")
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
      contentAsString(incorrectBody) must include("No request body found")
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
      contentAsString(incorrectBody) must include("No request body found")
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
      contentAsString(incorrectBody) must include("No request body found")
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

    "return product created if body is correctly structured but in odd order" in {
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

  "APIController PUT" should {}

  "APIController DELETE" should {}
}
